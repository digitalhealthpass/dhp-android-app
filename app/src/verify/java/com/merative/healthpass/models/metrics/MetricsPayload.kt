package com.merative.healthpass.models.metrics

import com.google.gson.JsonElement

data class MetricsPayload(
    val name: String?,
    val extract: JsonElement?,
    val countBy: JsonElement?,
)