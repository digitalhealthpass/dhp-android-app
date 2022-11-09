package com.merative.watson.healthpass.verificationengine.known

import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verificationengine.VerifyEngine

fun VerifyEngine.isKnown(): Boolean {
    val type = verifiableObject?.type ?: return false

    return type != VCType.UNKNOWN
}

class UnKnownTypeException : Exception("Unknown Credential")