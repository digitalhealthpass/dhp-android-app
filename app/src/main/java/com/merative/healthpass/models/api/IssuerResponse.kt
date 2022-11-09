package com.merative.healthpass.models.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement

data class IssuerResponse(
    val type: String?,
    val payload: JsonElement?,
    val keys: JsonArray?
) : BaseResponse()