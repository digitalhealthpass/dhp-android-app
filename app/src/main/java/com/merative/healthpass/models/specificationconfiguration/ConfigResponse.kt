package com.merative.healthpass.models.specificationconfiguration

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
  @SerializedName("type") var type: String? = null,
  @SerializedName("payload") var payload: ArrayList<Payload> = arrayListOf(),
)