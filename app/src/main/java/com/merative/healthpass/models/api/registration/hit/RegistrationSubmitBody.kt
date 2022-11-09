package com.merative.healthpass.models.api.registration.hit

data class RegistrationSubmitBody(
    val organization: String,
    val registrationCode: String,
    val publicKey: String = ""
)