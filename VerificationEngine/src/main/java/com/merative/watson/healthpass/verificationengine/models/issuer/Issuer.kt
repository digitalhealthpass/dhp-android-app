package com.merative.watson.healthpass.verificationengine.models.issuer

import com.google.gson.annotations.SerializedName

data class Issuer(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("created")
    val created: String?,
    @SerializedName("updated")
    val updated: String?,
    @SerializedName("publicKey")
    val publicKey: List<PublicKey>
)