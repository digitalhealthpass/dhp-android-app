package com.merative.watson.healthpass.verifiablecredential.models.cwt


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class RecoveryStatement(

    @JsonProperty("tg")
    val disease: String,

    @JsonProperty("fr")
    val dateOfFirstPositiveTest: String,

    @JsonProperty("co")
    val countryOfVaccination: String,

    @JsonProperty("is")
    val certificateIssuer: String,

    @JsonProperty("df")
    val certificateValidFrom: String,

    @JsonProperty("du")
    val certificateValidUntil: String,

    @JsonProperty("ci")
    val certificateIdentifier: String

) : Serializable {
    companion object {
        private val UTC_ZONE_ID: ZoneId = ZoneId.ofOffset("", ZoneOffset.UTC).normalized()
    }

    fun isCertificateNotValidAnymore(): Boolean? =
        certificateValidUntil.toZonedDateTimeOrUtcLocal()?.isBefore(ZonedDateTime.now())

    fun isCertificateNotValidSoFar(): Boolean? =
        certificateValidFrom.toZonedDateTimeOrUtcLocal()?.isAfter(ZonedDateTime.now())

    private fun String.toZonedDateTime(): ZonedDateTime? = try {
        ZonedDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toLocalDateTime(): LocalDateTime? = try {
        LocalDateTime.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toLocalDate(): LocalDate? = try {
        LocalDate.parse(this)
    } catch (error: Throwable) {
        null
    }

    private fun String.toZonedDateTimeOrUtcLocal(): ZonedDateTime? =
        this.toZonedDateTime()?.withZoneSameInstant(UTC_ZONE_ID) ?: this.toLocalDateTime()
            ?.atZone(UTC_ZONE_ID) ?: this.toLocalDate()?.atStartOfDay(UTC_ZONE_ID)
}