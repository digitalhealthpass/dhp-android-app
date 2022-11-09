package com.merative.watson.healthpass.verifiablecredential.models.jws

import android.util.Base64
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Inflater

/**
 * keep in mind if you use the other constructor with only String, it can return a Null object
 */
class JWS(JWSBody: String) {

    init {
        invoke(JWSBody)
    }

    @Keep
    @SerializedName("jws")
    lateinit var jws: String

    @Keep
    @SerializedName("headerString")
    var headerString: String? = null

    @Keep
    @SerializedName("payloadString")
    var payloadString: String? = null

    @Keep
    @SerializedName("signatureString")
    var signatureString: String? = null

    @Keep
    @SerializedName("header")
    var header: JWSHeader? = null

    @Keep
    @SerializedName("payload")
    var payload: JWSPayload? = null

    private fun invoke(JWSBody: String) {
        val headerString = JWSBody.split(".").getOrNull(0)
        val decodedHeader = decodeBase64(headerString)?.toString(charset("UTF-8"))

        val payloadString = JWSBody.split(".").getOrNull(1)
        val decompressedPayload = decompress(payloadString)

        val signatureString = JWSBody.split(".").getOrNull(2)

        return if (decodedHeader == null || decompressedPayload == null || signatureString == null) {
            // none
        } else {
            init(JWSBody, decodedHeader, decompressedPayload, signatureString)
        }
    }

    private fun decompress(message: String?): String? {
        var decompressedString: String? = null

        if (message == null) return decompressedString

        try {
            val bytes: ByteArray = decodeBase64(message) ?: return decompressedString

            val inflater = Inflater(true)
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            inflater.setInput(bytes)

            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            outputStream.close()
            decompressedString = outputStream.toString("UTF-8")

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return decompressedString
    }

    private fun decodeBase64(message: String?): ByteArray? {
        if (message == null) return null

        var updatedMessage =
            message.replace("-", "+")
                .replace("_", "/")

        when {
            updatedMessage.length % 4 == 2 -> updatedMessage += "=="
            updatedMessage.length % 4 == 3 -> updatedMessage += "="
        }

        return try {
            Base64.decode(updatedMessage, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun init(
        jws: String,
        headerString: String?,
        payloadString: String?,
        signatureString: String?
    ) {
        this.jws = jws
        this.headerString = headerString
        this.payloadString = payloadString
        this.signatureString = signatureString
        this.header = headerString?.let(::parse)
        this.payload = payloadString?.let(::parse)
    }

    companion object {

        val TAG = JWS::javaClass.name
    }

    init {
        invoke(JWSBody)
    }
}