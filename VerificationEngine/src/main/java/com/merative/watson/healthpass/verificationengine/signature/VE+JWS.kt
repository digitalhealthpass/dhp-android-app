package com.merative.watson.healthpass.verificationengine.signature

import android.util.Base64
import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.models.jws.JWS
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.VerifyEngine.Companion.TAG
import com.merative.watson.healthpass.verificationengine.exception.VerifyError
import com.merative.watson.healthpass.verificationengine.utils.extension.convertJwkToSecKey
import com.merative.watson.healthpass.verificationengine.utils.extension.convertToDer
import com.merative.watson.healthpass.verificationengine.utils.extension.verify
import java.security.Signature

@Throws(
    VerifyError.CredentialSignatureNoProperties::class,
)
fun VerifyEngine.hasValidSignature(jws: JWS): Boolean {
    //1. Get the jws basic properties
    val headerString = jws.jws.split(".").getOrNull(0)
    val payloadString = jws.jws.split(".").getOrNull(1)
    val signatureString = jws.signatureString

    if (headerString == null || payloadString == null || signatureString == null) {
        Log.e(TAG, "Signature - JWS - $jws")
        throw VerifyError.CredentialSignatureNoProperties
    }

    //2. Construct the data to verify using header and payload
    val headerAndPayloadString = "$headerString.$payloadString"
    val message = headerAndPayloadString.encodeToByteArray()

    Log.d(TAG, "Signature - JWS - headerAndPayloadString $headerAndPayloadString")

    //3. Convert the JWK format key to native SecKey format
    val signingPublicKeys = jwkSet?.map { convertJwkToSecKey(it) }
    if (signingPublicKeys == null) {
        Log.e(TAG, "Signature - JWS - signingPublicKeys = null")
        throw VerifyError.CredentialSignatureNoProperties
    }

    //4. Convert the signature string to data
    val decodedSignature = Base64.decode(signatureString, Base64.URL_SAFE)
    if (decodedSignature == null) {
        Log.e(TAG, "Signature - JWS - decodedSignature = null")
        throw VerifyError.CredentialSignatureNoProperties
    }

    //5. Check if the signatature matches the credential (actual signature verification)
    val sign = Signature.getInstance(SignatureAlgorithm.ECDSA256.value)
    val results = signingPublicKeys.map { key ->
        key?.let { sign.verify(it, message, decodedSignature.convertToDer()) } ?: false
    }

    //6. Check if verification was successful with any of the keys identified
    return results.contains(true)
}