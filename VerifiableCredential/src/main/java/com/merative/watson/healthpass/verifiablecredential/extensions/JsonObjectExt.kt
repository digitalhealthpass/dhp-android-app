package com.merative.watson.healthpass.verifiablecredential.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper

fun JsonObject.getStringOrNull(memberName: String): String? {
    return if (has(memberName) && get(memberName).isJsonPrimitive)
        get(memberName).asString
    else
        null
}

fun JsonObject.getIntOrNull(memberName: String): Int? {
    return if (has(memberName) && get(memberName).isJsonPrimitive)
        get(memberName).asInt
    else
        null
}

fun JsonObject.getBooleanOrNull(memberName: String): Boolean? {
    return if (has(memberName) && get(memberName).isJsonPrimitive)
        get(memberName).asBoolean
    else
        null
}

fun JsonObject.getBooleanOrDefault(memberName: String, default: Boolean): Boolean {
    return if (has(memberName) && get(memberName).isJsonPrimitive)
        get(memberName).asBoolean
    else
        default
}

fun <T> JsonObject.getListOrNull(memberName: String): List<T>? {
    val type = object : TypeToken<List<T>>() {}.type

    return if (has(memberName) && get(memberName).isJsonArray)
        GsonHelper.gson.fromJson<List<T>>(get(memberName).asJsonArray, type)
    else
        null
}

fun JsonObject.getJsonObjectOrNull(memberName: String): JsonObject? {
    return if (has(memberName) && get(memberName).isJsonObject)
        get(memberName).asJsonObject
    else
        null
}

fun JsonArray?.isNullOrEmpty(): Boolean {
    return this == null || this.size() == 0
}

fun JsonElement?.asJsonArrayOrNull(): JsonArray? {
    return if (this is JsonArray) {
        asJsonArray
    } else {
        null
    }
}

fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
    return if (this is JsonObject) {
        asJsonObject
    } else {
        null
    }
}