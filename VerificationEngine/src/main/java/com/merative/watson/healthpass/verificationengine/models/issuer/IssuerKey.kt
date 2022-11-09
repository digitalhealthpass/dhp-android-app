package com.merative.watson.healthpass.verificationengine.models.issuer

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class IssuerKey(
    @Keep
    @SerializedName("certificateType")
    val certificateType: String?,
    @Keep
    @SerializedName("country")
    val country: String?,
    @Keep
    @SerializedName("kid")
    val kid: String?,
    @Keep
    @SerializedName("rawData")
    val rawData: String?,
    @Keep
    @SerializedName("signature")
    val signature: String?,
    @Keep
    @SerializedName("thumbprint")
    val thumbprint: String?
)