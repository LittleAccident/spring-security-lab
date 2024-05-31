package ua.kpi.its.lab.security.dto

import kotlinx.serialization.Serializable

@Serializable
data class HospitalRequest(
    var name: String,
    var address: String,
    var profile: String,
    var openDate: String,
    var departments: Int,
    var beds: Int,
    var isChildDept: Boolean,
    var medicines: MedicineRequest
)
@Serializable
data class HospitalResponse(
    var id: Long,
    var name: String,
    var address: String,
    var profile: String,
    var openDate: String,
    var departments: Int,
    var beds: Int,
    var isChildDept: Boolean,
    var medicines: MedicineResponse
)
@Serializable

data class MedicineRequest(
    var name: String,
    var form: String,
    var manufacturer: String,
    var productionDate: String,
    var expiration: String,
    var price: String,
    var isPrescription: Boolean
)
@Serializable
data class MedicineResponse(
    var id: Long,
    var name: String,
    var form: String,
    var manufacturer: String,
    var productionDate: String,
    var expiration: String,
    var price: String,
    var isPrescription: Boolean
)
