package com.merative.watson.healthpass.verifiablecredential.models.veriableObject.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import java.lang.reflect.Type

class VODeserializer : JsonDeserializer<VerifiableObject?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): VerifiableObject? {
        if (json == null || (json.isJsonPrimitive && json.asString.contains("null"))) {
            return null
        }
        if (json.isJsonPrimitive) {
            return VerifiableObject(json.asString)
        }
        return VerifiableObject(json.toString())
    }
}