package com.merative.healthpass.models.api.registration.uploadCredential

data class UploadDocRequestBody(
    val link: String,
    val password: String,
    val name: String,
    val content: String
)

data class SubmitDataRequestBody(
    val organization: String,
    val publicKey: String,
    val documentId: String,
    val publicKeyType: String,
    val link: String//linkId
)