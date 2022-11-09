package com.merative.watson.healthpass.verifiablecredential.models.jws

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class JWSHeader(
    @Keep
    @SerializedName("alg")
    val alg: SignatureAlgorithm,
    @Keep
    @SerializedName("kid")
    val kid: String?,
    @Keep
    @SerializedName("typ")
    val typ: String?,
    @Keep
    @SerializedName("zip")
    val zip: CompressionAlgorithm
)