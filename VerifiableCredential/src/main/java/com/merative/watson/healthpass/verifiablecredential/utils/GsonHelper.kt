package com.merative.watson.healthpass.verifiablecredential.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.merative.watson.healthpass.verifiablecredential.interfaces.Exclude
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.serializer.VODeserializer
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.serializer.VOSerializer

object GsonHelper {

    private const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss"
    private const val DATE_FORMAT = "yyyy-MM-dd"

    val gson: Gson by lazy {
        GsonBuilder()
//        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//        .setDateFormat(DATE_TIME_FORMAT)
            .addSerializationExclusionStrategy(strategy)
            .registerTypeAdapter(VerifiableObject::class.java, VOSerializer())
            .registerTypeAdapter(VerifiableObject::class.java, VODeserializer())
            .disableHtmlEscaping()
            .create()
    }


    private val strategy: ExclusionStrategy = object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(field: FieldAttributes): Boolean {
            return field.getAnnotation(Exclude::class.java) != null
        }
    }
}