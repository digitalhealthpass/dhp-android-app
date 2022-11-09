package com.merative.watson.healthpass.verifiablecredential.models.veriableObject

import android.util.Log

fun VerifiableObject.isValidJWS(JWSBody: String): Boolean {
    val bodySize = JWSBody.split(".").size
    if (bodySize != VerifiableObject.JWS_BODY_SIZE) {
        Log.d(VerifiableObject.TAG, "JWS body is invalid.")
        return false
    }
    return true
}