package com.merative.healthpass.models.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.merative.healthpass.models.region.Env
import java.lang.reflect.Type

class EnvSerializer : JsonSerializer<Env> {
    override fun serialize(
        any: Env,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", any.name)
        jsonObject.addProperty("url", any.url)
        jsonObject.addProperty("issuerId", any.issuerId)
        jsonObject.addProperty("title", any.title)
        jsonObject.addProperty("description", any.description)
        jsonObject.addProperty("titleRes", any.titleRes)
        jsonObject.addProperty("descriptionRes", any.descriptionRes)
        jsonObject.addProperty("isProd", any.isProd)
        return jsonObject
    }
}