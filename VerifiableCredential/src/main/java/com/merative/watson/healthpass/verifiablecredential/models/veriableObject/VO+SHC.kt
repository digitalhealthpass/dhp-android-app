package com.merative.watson.healthpass.verifiablecredential.models.veriableObject

import android.util.Log

fun VerifiableObject.isValidSHC(message: String): Boolean {
    var result = message.startsWith(VerifiableObject.SHC_PREFIX)

    if (!result) {
        try {
            parseSHC(message)
        } catch (e: Exception) {
            result = false
        }
    }
    if (!result) Log.d(VerifiableObject.TAG, "SHC header not found.")
    return result
}

fun VerifiableObject.parseSHC(message: String): String? {
    val shcBody = message.removePrefix(VerifiableObject.SHC_PREFIX)

    if (shcBody.length % 2 != 0) {
        Log.d(VerifiableObject.TAG, "SHC body is invalid.")
        return null
    }

    val shcBodyArray = Regex("..").findAll(shcBody)
    val jwsBodyStringArray = shcBodyArray.map { it.value.toInt() + 45 }.map { it.toChar() }

    if (shcBodyArray.toList().size != jwsBodyStringArray.toList().size) {
        Log.d(VerifiableObject.TAG, "Data mismatch")
        return null
    }

    return jwsBodyStringArray.joinToString("")
}