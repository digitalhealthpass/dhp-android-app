package com.merative.watson.healthpass.verificationengine.models.issuer

import com.google.gson.annotations.SerializedName
import com.merative.watson.healthpass.verificationengine.utils.Base64Decoder

data class JWK(
    @SerializedName("kty")
    val kty: String,
    @SerializedName("crv")
    val crv: String?,
    @SerializedName("alg")
    val alg: String?,
    @SerializedName("ext")
    val ext: Boolean?,
    @SerializedName("kid")
    val kid: String?,
    @SerializedName("keyOps")
    val keyOps: List<String>?,
    @SerializedName("use")
    val use: String?,
    @SerializedName("k")
    val k: String?,
    @SerializedName("d")
    val d: String?,
    @SerializedName("n")
    val n: String?,
    @SerializedName("e")
    val e: String?,
    @SerializedName("p")
    val p: String?,
    @SerializedName("q")
    val q: String?,
    @SerializedName("dp")
    val dp: String?,
    @SerializedName("dq")
    val dq: String?,
    @SerializedName("qi")
    val qi: String?,
    @SerializedName("x")
    val x: String?,
    @SerializedName("y")
    val y: String?
) {

    @Throws(JWKError::class)
    fun asP256PublicKey(): String {
        if (kty != "EC") {
            throw JWKError.IncorrectKeyType("EC", kty)
        }

        if (crv != "P-256") {
            throw JWKError.UnsupportedCurve(crv)
        }

        return asRawPublicKey()

        // TODO: in-progress code
        /**
        val rawKey = asRawPublicKey()
        return P256.Signing.PublicKey(rawRepresentation: rawKey)
        */
    }

    @Throws(JWKError::class)
    private fun asRawPublicKey(): String {
        val x: String = x ?: throw JWKError.MissingXComponent
        val y: String = y ?: throw JWKError.MissingYComponent

        val xData = Base64Decoder.decode(x)?.toString(charset("UTF-8"))
        val yData = Base64Decoder.decode(y)?.toString(charset("UTF-8"))

        var result = ""
        if (xData != null) result = xData
        if (yData != null) result += yData

        return result
    }
}