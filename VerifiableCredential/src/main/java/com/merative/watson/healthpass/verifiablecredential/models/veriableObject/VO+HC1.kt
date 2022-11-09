package com.merative.watson.healthpass.verifiablecredential.models.veriableObject

import android.util.Log

fun VerifiableObject.isValidHC1(message: String): Boolean {
    val result = message.startsWith(VerifiableObject.HC1_PREFIX)
    if (!result) Log.d(VerifiableObject.TAG, "HC1 header not found.")
    return result
}

fun VerifiableObject.parseHC1(message: String): String {
    return message.removePrefix(VerifiableObject.HC1_PREFIX)
}