package com.merative.healthpass.models.api.metric

data class SubmitMetricObject(
    val orgId: String?,
    val verDID: String?,
    val customerId: String?,
    val scans: List<ScanObject>,
)