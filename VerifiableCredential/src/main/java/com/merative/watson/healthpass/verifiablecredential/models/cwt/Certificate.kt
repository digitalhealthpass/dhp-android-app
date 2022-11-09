package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*

/**
 * CBOR structure of the certificate
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Certificate(

    @JsonProperty("ver")
    val schemaVersion: String = "",

    @JsonProperty("nam")
    val person: Person? = null,

    @JsonProperty("dob")
    val dateOfBirth: String = "",

    @JsonProperty("v")
    val vaccinations: List<Vaccination>? = emptyList(),

    @JsonProperty("t")
    val tests: List<Test>? = emptyList(),

    @JsonProperty("r")
    val recoveryStatements: List<RecoveryStatement>? = emptyList()

) : Serializable {

    fun getDgci(): String {
        return try {
            return when {
                vaccinations?.isNotEmpty() == true -> vaccinations.last().certificateIdentifier
                tests?.isNotEmpty() == true -> tests.last().certificateIdentifier
                recoveryStatements?.isNotEmpty() == true -> recoveryStatements.last().certificateIdentifier
                else -> ""
            }
        } catch (ex: Exception) {
            ""
        }
    }

    fun getIssuingCountry(): String = try {
        when {
            vaccinations?.isNotEmpty() == true -> vaccinations.last().countryOfVaccination
            tests?.isNotEmpty() == true -> tests.last().countryOfVaccination
            recoveryStatements?.isNotEmpty() == true -> recoveryStatements.last().countryOfVaccination
            else -> ""
        }
    } catch (ex: Exception) {
        ""
    }.toLowerCase(Locale.ROOT)

    fun getType(): CertificateType {
        if (vaccinations?.isNotEmpty() == true)
            return CertificateType.VACCINATION
        if (tests?.isNotEmpty() == true) return CertificateType.TEST
        if (recoveryStatements?.isNotEmpty() == true) return CertificateType.RECOVERY
        return CertificateType.UNKNOWN
    }
}
