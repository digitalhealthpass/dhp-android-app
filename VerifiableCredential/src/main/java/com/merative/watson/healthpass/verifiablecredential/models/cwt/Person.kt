package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(

    @JsonProperty("fnt")
    val standardisedFamilyName: String = "",

    @JsonProperty("fn")
    val familyName: String? = "",

    @JsonProperty("gnt")
    val standardisedGivenName: String? = "",

    @JsonProperty("gn")
    val givenName: String? = ""

) : Serializable