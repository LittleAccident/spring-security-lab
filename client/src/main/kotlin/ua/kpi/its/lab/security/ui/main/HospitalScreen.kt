package ua.kpi.its.lab.security.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import ua.kpi.its.lab.security.dto.MedicineRequest
import ua.kpi.its.lab.security.dto.HospitalRequest
import ua.kpi.its.lab.security.dto.HospitalResponse

@Composable
fun HospitalScreen(
    token: String,
    scope: CoroutineScope,
    client: HttpClient,
    snackbarHostState: SnackbarHostState
) {
    var hospitals by remember { mutableStateOf<List<HospitalResponse>>(listOf()) }
    var loading by remember { mutableStateOf(false) }
    var openDialog by remember { mutableStateOf(false) }
    var selectedHospital by remember { mutableStateOf<HospitalResponse?>(null) }

    LaunchedEffect(token) {
        loading = true
        delay(1000)
        hospitals = withContext(Dispatchers.IO) {
            try {
                val response = client.get("http://localhost:8080/hospitals") {
                    bearerAuth(token)
                }
                loading = false
                response.body()
            }
            catch (e: Exception) {
                val msg = e.toString()
                snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                hospitals
            }
        }
    }

    if (loading) {
        LinearProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedHospital = null
                    openDialog = true
                },
                content = {
                    Icon(Icons.Filled.Add, "Add Hospital")
                }
            )
        }
    ) {
        if (hospitals.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Zero Hospitals to show", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        else {
            LazyColumn(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hospitals) { hospital ->
                    HospitalItem(
                        hospital = hospital,
                        onEdit = {
                            selectedHospital = hospital
                            openDialog = true
                        },
                        onRemove = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    try {
                                        val response = client.delete("http://localhost:8080/hospitals/${hospital.id}") {
                                            bearerAuth(token)
                                        }
                                        require(response.status.isSuccess())
                                    }
                                    catch(e: Exception) {
                                        val msg = e.toString()
                                        snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                                    }
                                }

                                loading = true

                                hospitals = withContext(Dispatchers.IO) {
                                    try {
                                        val response = client.get("http://localhost:8080/hospitals") {
                                            bearerAuth(token)
                                        }
                                        loading = false
                                        response.body()
                                    }
                                    catch (e: Exception) {
                                        val msg = e.toString()
                                        snackbarHostState.showSnackbar(msg, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                                        hospitals
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        if (openDialog) {
            HospitalDialog(
                hospital = selectedHospital,
                token = token,
                scope = scope,
                client = client,
                onDismiss = {
                    openDialog = false
                },
                onError = {
                    scope.launch {
                        snackbarHostState.showSnackbar(it, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                    }
                },
                onConfirm = {
                    openDialog = false
                    loading = true
                    scope.launch {
                        hospitals = withContext(Dispatchers.IO) {
                            try {
                                val response = client.get("http://localhost:8080/hospitals") {
                                    bearerAuth(token)
                                }
                                loading = false
                                response.body()
                            }
                            catch (e: Exception) {
                                loading = false
                                hospitals
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun HospitalDialog(
    hospital: HospitalResponse?,
    token: String,
    scope: CoroutineScope,
    client: HttpClient,
    onDismiss: () -> Unit,
    onError: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val medicine = hospital?.medicines

    var name by remember { mutableStateOf(hospital?.name ?: "") }
    var address by remember { mutableStateOf(hospital?.address ?: "") }
    var profile by remember { mutableStateOf(hospital?.profile ?: "") }
    var openDate by remember { mutableStateOf(hospital?.openDate ?: "") }
    var departments by remember { mutableStateOf(hospital?.departments?.toString() ?: "") }
    var beds by remember { mutableStateOf(hospital?.beds?.toString() ?: "") }
    var isChildDept by remember { mutableStateOf(hospital?.isChildDept ?: false) }
    var medicineName by remember { mutableStateOf(medicine?.name ?: "") }
    var medicineForm by remember { mutableStateOf(medicine?.form ?: "") }
    var medicineManufacturer by remember { mutableStateOf(medicine?.manufacturer ?: "") }
    var medicineProductionDate by remember { mutableStateOf(medicine?.productionDate ?: "") }
    var medicineExpiration by remember { mutableStateOf(medicine?.expiration ?: "") }
    var medicinePrice by remember { mutableStateOf(medicine?.price ?: "") }
    var medicineIsPrescription by remember { mutableStateOf(medicine?.isPrescription ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp).wrapContentSize()) {
            Column(
                modifier = Modifier.padding(16.dp, 8.dp).width(IntrinsicSize.Max).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hospital == null) {
                    Text("Add Hospital")
                }
                else {
                    Text("Change existing hospital")
                }

                HorizontalDivider()
                Text("Hospital Information")
                TextField(name, { name = it }, label = { Text("Name") })
                TextField(address, { address = it }, label = { Text("Address") })
                TextField(profile, { profile = it }, label = { Text("Profile") })
                TextField(openDate, { openDate = it }, label = { Text("Opening date") })
                TextField(departments, { departments = it }, label = { Text("# of Departments") })
                TextField(beds, { beds = it }, label = { Text("Hospital Beds") })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(isChildDept, { isChildDept = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Child department")
                }

                HorizontalDivider()
                Text("Medicaments/Pharmacy")
                TextField(medicineName, { medicineName = it }, label = { Text("Name") })
                TextField(medicineForm, { medicineForm = it }, label = { Text("Form") })
                TextField(medicineManufacturer, { medicineManufacturer = it }, label = { Text("Manufacturer") })
                TextField(medicineProductionDate, { medicineProductionDate = it }, label = { Text("Production date") })
                TextField(medicineExpiration, { medicineExpiration = it }, label = { Text("Expiration date") })
                TextField(medicinePrice, { medicinePrice = it }, label = { Text("Price") })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(medicineIsPrescription, { medicineIsPrescription = it })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Prescription")
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            scope.launch {
                                try {
                                    val request = HospitalRequest(
                                        name, address, profile, openDate, departments.toInt(), beds.toInt(), isChildDept,
                                        MedicineRequest(
                                            medicineName, medicineForm, medicineManufacturer, medicineProductionDate,
                                            medicineExpiration, medicinePrice, medicineIsPrescription
                                        )
                                    )
                                    val response = if (hospital == null) {
                                        client.post("http://localhost:8080/hospitals") {
                                            bearerAuth(token)
                                            setBody(request)
                                            contentType(ContentType.Application.Json)
                                        }
                                    } else {
                                        client.put("http://localhost:8080/hospitals/${hospital.id}") {
                                            bearerAuth(token)
                                            setBody(request)
                                            contentType(ContentType.Application.Json)
                                        }
                                    }
                                    require(response.status.isSuccess())
                                    onConfirm()
                                }
                                catch (e: Exception) {
                                    val msg = e.toString()
                                    onError(msg)
                                }
                            }
                        }
                    ) {
                        if (hospital == null) {
                            Text("Create")
                        }
                        else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalItem(hospital: HospitalResponse, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(shape = CardDefaults.elevatedShape, elevation = CardDefaults.elevatedCardElevation()) {
        ListItem(
            overlineContent = {
                Text(hospital.name)
            },
            headlineContent = {
                Text(hospital.address)
            },
            supportingContent = {
                Text("$${hospital.medicines.price}")
            },
            trailingContent = {
                Row(modifier = Modifier.padding(0.dp, 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onEdit)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onRemove)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        )
    }
}
