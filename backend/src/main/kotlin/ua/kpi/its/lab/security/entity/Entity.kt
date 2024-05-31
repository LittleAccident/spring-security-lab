package ua.kpi.its.lab.security.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "hospitals")
class Hospital(
    @Column
    var name: String,

    @Column
    var address: String,

    @Column
    var profile: String,

    @Column
    var openDate: Date,

    @Column
    var departments: Int,

    @Column
    var beds: Int,

    @Column
    var isChildDept: Boolean,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "medicine_id", referencedColumnName = "id")
    var medicines: Medicine
) : Comparable<Hospital> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = -1

    override fun compareTo(other: Hospital): Int {
        val equal = this.name == other.name && this.openDate.time == other.openDate.time
        return if (equal) 0 else 1
    }

    override fun toString(): String {
        return "Hospital(name=$name, openDate=$openDate, medicines=$medicines)"
    }
}

@Entity
@Table(name = "medicines")
class Medicine(
    @Column
    var name: String,

    @Column
    var form: String,

    @Column
    var manufacturer: String,

    @Column
    var productionDate: Date,

    @Column
    var expiration: Date,

    @Column
    var price: BigDecimal,

    @Column
    var isPrescription: Boolean,

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    var hospital: Hospital? = null
) : Comparable<Medicine> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = -1

    override fun compareTo(other: Medicine): Int {
        val equal = this.name == other.name && this.expiration.time == other.expiration.time
        return if (equal) 0 else 1
    }

    override fun toString(): String {
        return "Medicine(name=$name, expiration=$expiration)"
    }
}
