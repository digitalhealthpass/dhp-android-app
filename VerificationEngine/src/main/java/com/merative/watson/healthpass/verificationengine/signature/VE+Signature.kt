package com.merative.watson.healthpass.verificationengine.signature

import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.exception.VerifyError

fun VerifyEngine.hasValidSignature(): Boolean {
    val type = verifiableObject?.type

    if (type == null) {
        Log.e(this.javaClass.simpleName, "VerifyEngine - Signature - Unknown Type")
        return false
    }

    val jws = verifiableObject?.jws
    val credential = verifiableObject?.credential
    val cose = verifiableObject?.cose

    val valid = if (jws != null) {
        hasValidSignature(jws)
    } else if ((type == VCType.IDHP || type == VCType.GHP|| type == VCType.VC) && credential != null) {
        hasValidIDHPSignature(credential)
    } else if (cose != null) {
        hasValidSignature(cose)
    } else {
        Log.e(this.javaClass.simpleName, "Signature - Unknown Object")
        return false
    }

    if (!valid) {
        throw VerifyError.CredentialSignatureInvalidSignatureData
    }

    return valid
}