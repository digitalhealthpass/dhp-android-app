package com.merative.watson.healthpass.verifiablecredential.models.veriableObject.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import java.lang.reflect.Type

class VOSerializer : JsonSerializer<VerifiableObject> {
    override fun serialize(any: VerifiableObject, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (any.rawString.startsWith("{")) {
            any.rawString.toJsonElement()
        } else {
            JsonPrimitive(any.rawString)
        }
    }
}