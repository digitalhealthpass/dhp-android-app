package com.merative.healthpass.models

import com.google.gson.JsonElement
import com.merative.healthpass.models.api.BaseResponse

data class RevocationStatusResponse(
    val payload: JsonElement?,
) : BaseResponse()