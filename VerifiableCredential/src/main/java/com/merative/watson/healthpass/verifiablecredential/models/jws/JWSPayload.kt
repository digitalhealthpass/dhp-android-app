package com.merative.watson.healthpass.verifiablecredential.models.jws

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential

data class JWSPayload(
    @Keep
    @SerializedName("sub")
    val sub: String?,
    @Keep
    @SerializedName("jti")
    val jti: String?,
    @Keep
    @SerializedName("iss")
    val iss: String?,
    @Keep
    @SerializedName("nbf")
    val nbf: Double?,
    @Keep
    @SerializedName("iat")
    val iat: Double?,
    @Keep
    @SerializedName("exp")
    val exp: Double?,
    @Keep
    @SerializedName("nonce")
    val nonce: String?,
    @Keep
    @SerializedName("vc")
    val vc: Credential?
)