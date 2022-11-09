package com.merative.healthpass.models.serializer


import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.merative.healthpass.models.region.Env
import java.lang.reflect.Type

class EnvDeserializer : JsonDeserializer<Env?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Env? {
        return if (json == null || (json.isJsonPrimitive && json.asString.contains("null"))) {
            null
        } else {
            val jsonObject = json as JsonObject
            Env.values().firstOrNull {
                val res = when {
                    jsonObject.has("name") -> {
                        it.name == jsonObject.getAsJsonPrimitive("name").asString
                    }
                    jsonObject.has("title") -> {
                        it.title == jsonObject.getAsJsonPrimitive("title").asString
                    }
                    jsonObject.has("titleRes") -> {
                        it.titleRes == jsonObject.getAsJsonPrimitive("titleRes").asInt
                    }
                    else -> {
                        it.url == jsonObject.getAsJsonPrimitive("url")?.asString
                    }
                }
                res
            }
        }
    }
}