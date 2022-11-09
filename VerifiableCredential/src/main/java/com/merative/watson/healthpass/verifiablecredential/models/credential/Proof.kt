package com.merative.watson.healthpass.verifiablecredential.models.credential

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Proof(
    @SerializedName("created")
    var created: String?,
    @SerializedName("creator")
    var creator: String?,
    @SerializedName("nonce")
    var nonce: String?,
    @SerializedName("signatureValue")
    var signatureValue: String?,
    @SerializedName("type")
    var type: String?,
    @SerializedName("jws")
    var jws: String?,
    @SerializedName("verificationMethod")
    var verificationMethod: String?,
    @SerializedName("proofPurpose")
    var proofPurpose: String?,
    @SerializedName("domain")
    var domain: String?,
    @SerializedName("challenge")
    var challenge: String?,
    @SerializedName("issuerData")
    var issuerData: String?,
    @SerializedName("attributes")
    var attributes: String?,
    @SerializedName("signature")
    var signature: String?,
    @SerializedName("signatureCorrectnessProof")
    var signatureCorrectnessProof: String?
) : Parcelable
