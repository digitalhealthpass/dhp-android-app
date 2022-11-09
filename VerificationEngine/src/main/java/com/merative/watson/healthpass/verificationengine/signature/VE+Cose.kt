package com.merative.watson.healthpass.verificationengine.signature

import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.models.cose.Algorithm
import com.merative.watson.healthpass.verifiablecredential.models.cose.Cose
import com.merative.watson.healthpass.verifiablecredential.models.cose.CoseType
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.VerifyEngine.Companion.TAG
import com.merative.watson.healthpass.verificationengine.utils.extension.convertToDer
import com.merative.watson.healthpass.verificationengine.utils.extension.stringPkToCertificate
import com.merative.watson.healthpass.verificationengine.utils.extension.verify
import com.upokecenter.cbor.CBORObject
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.RSAPublicKeySpec

fun VerifyEngine.hasValidSignature(cose: Cose): Boolean {
    return when (cose.type) {
        CoseType.SIGNATURE_1 -> hasCoseSign1ValidSignature(cose)      //fully different type????
        else -> {
            Log.e(TAG, "Signature - COSE - Sign messages are not yet supported")
            false
        }
    }
}

private fun VerifyEngine.hasCoseSign1ValidSignature(cose: Cose): Boolean {
    val signedData = cose.signatureStruct
    return if (signedData == null) {
        Log.e(TAG, "Signature - COSE - Cannot create Sign1 structure")
        false
    } else {
        val publicKeys = issuerKeys?.map { it.rawData?.let(::stringPkToCertificate)?.publicKey }
        val results = publicKeys?.map { key ->
            Log.d(TAG, "FOUND PUBLIC KEY $key")
            key?.let { verifySignature(cose, signedData, cose.signature, key) }
        }

        results?.contains(true) ?: false
    }
}

private fun verifySignature(
    cose: Cose,
    signedData: ByteArray,
    rawSignature: CBORObject,
    verificationKey: PublicKey
): Boolean {
    try {
        val coseSignature = rawSignature.GetByteString().convertToDer()
        return when (cose.protectedHeader?.algorithm) {
            Algorithm.ES_256 -> {
                Signature
                    .getInstance(SignatureAlgorithm.ECDSA256.value)
                    .verify(verificationKey, signedData, coseSignature)
            }
            Algorithm.PS_256 -> {
                val bytes =
                    SubjectPublicKeyInfo.getInstance(verificationKey.encoded).publicKeyData.bytes
                val rsaPublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(bytes)
                val spec = RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent)
                val key = KeyFactory.getInstance("RSA").generatePublic(spec)

                Signature
                    .getInstance(SignatureAlgorithm.RSA256_PSS.value)
                    .verify(key, signedData, coseSignature)
            }
            else -> false
        }
    } catch (ex: Exception) {
        Log.e(TAG, "Signature - COSE - Signature verification failed with exception", ex)
        return false
    }
}