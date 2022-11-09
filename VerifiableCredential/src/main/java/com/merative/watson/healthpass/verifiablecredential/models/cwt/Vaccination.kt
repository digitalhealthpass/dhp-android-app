package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Vaccination(

    @JsonProperty("tg")
    val disease: String = "",

    @JsonProperty("vp")
    val vaccine: String = "",

    @JsonProperty("mp")
    val medicinalProduct: String = "",

    @JsonProperty("ma")
    val manufacturer: String = "",

    @JsonProperty("dn")
    val doseNumber: Int = -1,

    @JsonProperty("sd")
    val totalSeriesOfDoses: Int = -1,

    @JsonProperty("dt")
    val dateOfVaccination: String = "",

    @JsonProperty("co")
    val countryOfVaccination: String = "",

    @JsonProperty("is")
    val certificateIssuer: String = "",

    @JsonProperty("ci")
    val certificateIdentifier: String = ""

) : Serializable