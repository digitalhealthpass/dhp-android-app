package com.merative.healthpass.models.credential

import com.google.gson.JsonObject

class DisplayField(
    val field: String,
    val displayValue: JsonObject,
    val sectionIndex: Int? = null,
    val sectionTitle: JsonObject? = null,
    val valueMapper: String? = null
)