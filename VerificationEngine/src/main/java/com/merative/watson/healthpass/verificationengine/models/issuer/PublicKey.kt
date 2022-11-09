package com.merative.watson.healthpass.verificationengine.models.issuer

import com.google.gson.annotations.SerializedName

data class PublicKey(
    @SerializedName("id")
    val id: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("controller")
    val controller: String?,
    @SerializedName("publicKeyJwk")
    val publicKeyJwk: JWK?
)