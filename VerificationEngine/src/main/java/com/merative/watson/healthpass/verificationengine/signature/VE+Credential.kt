package com.merative.watson.healthpass.verificationengine.signature

import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import com.merative.watson.healthpass.verifiablecredential.extensions.toJSONObject
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.VerifyEngine.Companion.TAG
import com.merative.watson.healthpass.verificationengine.exception.VerifyError
import com.merative.watson.healthpass.verificationengine.utils.extension.convertJwkToSecKey
import com.merative.watson.healthpass.verificationengine.utils.extension.verify
import org.json.JSONObject
import java.security.Signature

@Throws(
    VerifyError.CredentialSignatureNoProperties::class,
    VerifyError.CredentialSignatureInvalidSignatureData::class,
    VerifyError.CredentialSignatureInvalidCredentialData::class,
    VerifyError.CredentialSignatureUnsupportedKey::class,
)
fun VerifyEngine.hasValidIDHPSignature(credential: Credential): Boolean {

    //1. Get the signature from the credential for verification
    val signature = credential.proof?.signatureValue
    if (signature == null) {
        Log.e(TAG, "Signature - Credential - signature = null")
        throw VerifyError.CredentialSignatureNoProperties
    }

    //2. Convert the signature string to data
    val decodedSignature = android.util.Base64.decode(signature, android.util.Base64.URL_SAFE)
    if (decodedSignature == null) {
        Log.e(TAG, "Signature - Credential - decodedSignature = null")
        throw VerifyError.CredentialSignatureInvalidSignatureData
    }

    //3. Get the credential dictionary removing the signature value and any obfuscation
    val unsignedCredentialJson = credential.stringfy().toJSONObject()
    //Remove signatureValue for verification process

    val proof = unsignedCredentialJson?.getJSONObject("proof")
    if (proof != null) {
        proof.remove("signatureValue")
        unsignedCredentialJson.put("proof", proof)
    }

    val credentialSubject = unsignedCredentialJson?.getJSONObject("credentialSubject")
    if (credentialSubject != null) {
        val keys = mutableListOf<String>()
        val iterator = credentialSubject.keys()
        while (iterator.hasNext()) {
            keys.add(iterator.next())
        }
        keys.sort()
        val sortedCredentialSubject = JSONObject()
        keys.forEach {
            val jsonObject = credentialSubject.get(it)
            sortedCredentialSubject.put(it, jsonObject)
        }
        unsignedCredentialJson.put("credentialSubject", sortedCredentialSubject)
    }

    //Remove obfuscation for verification process
    unsignedCredentialJson?.remove("obfuscation")

    val unsignedRawData =
        unsignedCredentialJson?.toString()?.replace("\\/", "/")?.encodeToByteArray()
    if (unsignedRawData == null) {
        Log.e(TAG, "Signature - Credential - unsignedRawData = null")
        throw VerifyError.CredentialSignatureInvalidCredentialData
    }

    val keyID = credential.proof?.creator
    if (keyID == null) {
        Log.e(TAG, "Signature - Credential - keyID = null")
    }

    val publicKeys = issuer?.publicKey?.filter { it.id == keyID }
    if (publicKeys == null) {
        Log.e(TAG, "Signature - Credential - publicKeys = null")
        throw VerifyError.CredentialSignatureUnsupportedKey
    }

    //5. Convert the public key from issuer to JWK format
    val publicKeyJWKs = publicKeys.map { it.publicKeyJwk }

    //6. Convert the JWK format key to native SecKey format
    val nativePublicKeys = publicKeyJWKs.map { it?.let(::convertJwkToSecKey) }

    //7. Check if the signature matches the credential (actual signature verification)
    val sign = Signature.getInstance(SignatureAlgorithm.ECDSA256.value)
    val results = nativePublicKeys.map { key ->
        key?.let { sign.verify(it, unsignedRawData, decodedSignature) } ?: false
    }

    //8. Check if verification was successful with any of the keys identified
    return results.contains(true)
}