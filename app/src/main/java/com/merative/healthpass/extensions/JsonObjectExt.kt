package com.merative.healthpass.extensions

import com.google.gson.JsonObject
import com.merative.healthpass.models.credential.AsymmetricKey

fun JsonObject?.sign(asymmetricKey: AsymmetricKey?): String? {
    return this?.toString()?.sign(asymmetricKey)
}