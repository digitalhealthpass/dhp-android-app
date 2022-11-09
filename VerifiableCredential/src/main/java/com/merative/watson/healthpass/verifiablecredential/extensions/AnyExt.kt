package com.merative.watson.healthpass.verifiablecredential.extensions

import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import org.json.JSONException
import org.json.JSONObject

fun <R : Any> R.javaName(): String =
    if (!javaClass.simpleName.isNullOrEmpty()) javaClass.simpleName else javaClass.name

@Throws(
    JsonParseException::class,
    JsonSyntaxException::class
)
inline fun <reified T : Any> parse(json: String, gson: Gson = GsonHelper.gson): T {
    val type = object : TypeToken<T>() {}.type
    return gson.fromJson(json, type)
}

@Throws(
    JsonParseException::class,
    JsonSyntaxException::class
)
fun String.toMap(): Map<String, Any> = parse<Map<String, Any>>(this)

fun Any.toJsonElement(gson: Gson = GsonHelper.gson): JsonElement = if (this is String) {
    JsonParser.parseString(this)
} else {
    gson.toJsonTree(this)
}

/**
 * convert to [JSONObject] or null if it fails
 */
fun String.toJSONObject(): JSONObject? {
    return try {
        JSONObject(this)
    } catch (ex: JSONException) {
        Log.e(javaName(), "failed to convert to JSONObject", ex)
        null
    }
}

/**convert the object to json string*/
fun Any.stringfy(gson: Gson = GsonHelper.gson): String = gson.toJson(this)