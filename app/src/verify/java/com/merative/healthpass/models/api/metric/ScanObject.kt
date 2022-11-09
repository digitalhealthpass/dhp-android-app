package com.merative.healthpass.models.api.metric

data class ScanObject(
    val issuerDID: String?,
    val issuerName: String?,
    val credentialSpec: String?,
    val credentialType: String?,
    val datetime: String?,
    val scanResult: String?,
    val total: Int
)