package com.merative.watson.healthpass.verifiablecredential.models

enum class VCType(val value: String) {
    SHC("smarthealth.cards"),
    IDHP("IBMDigitalHealthPass"),
    DIVOC("DIVOC"),
    DCC("EU DCC"),
    GHP("GoodHealthPass"),
    VC("VerifiableCredential"),
    CONTACT("Contact"),

    UNKNOWN("Unknown");

    val displayValue: String
        get() {
            return when {
                this == SHC -> "SMART® Health Card"
                this == IDHP -> "Open Digital Health Pass"
                this == DIVOC -> "DIVOC's India Cowin"
                this == DCC -> "EU Digital COVID Certificate"
                this == GHP -> "Good Health Pass"
                this == VC -> "Verifiable Credential"
                this == CONTACT -> "Contact"
                else -> "unknown"
            }
        }
}