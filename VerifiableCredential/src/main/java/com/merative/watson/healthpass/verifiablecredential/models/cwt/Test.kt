package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class Test(

    @JsonProperty("tg")
    val disease: String,

    @JsonProperty("tt")
    val typeOfTest: String,

    @JsonProperty("nm")
    val testName: String?,

    @JsonProperty("ma")
    val testNameAndManufacturer: String?,

    @JsonProperty("sc")
    val dateTimeOfCollection: String,

    @JsonProperty("dr")
    val dateTimeOfTestResult: String?,

    @JsonProperty("tr")
    val testResult: String,

    @JsonProperty("tc")
    val testingCentre: String,

    @JsonProperty("co")
    val countryOfVaccination: String,

    @JsonProperty("is")
    val certificateIssuer: String,

    @JsonProperty("ci")
    val certificateIdentifier: String

) : Serializable {

    fun isResultNegative(): Boolean = testResult == TestResult.NOT_DETECTED.value

    fun isDateInThePast(): Boolean =
        parseToUtcTimestamp(dateTimeOfCollection).isBefore(OffsetDateTime.now())

    fun getTestResultType(): TestResult {
        return when (testResult) {
            TestResult.DETECTED.value -> TestResult.DETECTED
            TestResult.NOT_DETECTED.value -> TestResult.NOT_DETECTED
            else -> TestResult.NOT_DETECTED
        }
    }

    private fun parseToUtcTimestamp(value: String?): OffsetDateTime {
        if (value.isNullOrEmpty()) {
            return OffsetDateTime.MAX
        }

        return try {
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value, OffsetDateTime::from)
                .withOffsetSameInstant(ZoneOffset.UTC)
        } catch (ex: Exception) {
            OffsetDateTime.MAX
        }
    }

    enum class TestResult(val value: String) {
        DETECTED("260373001"),
        NOT_DETECTED("260415000")
    }
}
