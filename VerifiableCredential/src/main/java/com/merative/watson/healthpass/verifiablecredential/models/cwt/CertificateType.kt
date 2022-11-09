package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
enum class CertificateType(val displayValue: String) {
    UNKNOWN("Unknown"),
    VACCINATION("Vaccination"),
    RECOVERY("Recovery"),
    TEST("Test")
}