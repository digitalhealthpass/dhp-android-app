package com.merative.healthpass.models

import com.merative.watson.healthpass.verifiablecredential.extensions.SERVER_DATE_FORMAT
import com.merative.watson.healthpass.verifiablecredential.extensions.format
import java.util.*

data class Metric(
    val status: String?,
    val type: String?,
    val issuerId: String?,
    val issuerName: String?,
    val verifierId: String?,
    val organizationId: String?,
    val customerId: String?,
    val credentialSpec: String?,
    val timestamp: String? = Date().format(SERVER_DATE_FORMAT, Locale.US),
)