/**
 * Original file is https://github.com/advantagefse/json-logic-kotlin/blob/master/src/main/kotlin/eu/afse/jsonlogic/Utils.kt
 */

package com.merative.watson.healthpass.verificationengine.rules.jsonlogic

import com.google.gson.reflect.TypeToken
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper

internal val String?.parse: Any?
    get() = try {
        GsonHelper.gson.fromJson(this, Map::class.java)
    } catch (e: Exception) {
        try {
            GsonHelper.gson.fromJson(this, List::class.java)
        } catch (e: Exception) {
            this
        }
    }

// added this method to transform data structures to json
internal val Any?.jsonify: String
    get() =
        when (this) {
            is Map<*, *> -> {
                val gsonType = object : TypeToken<Map<*, *>>() {}.type
                val gsonString: String = GsonHelper.gson.toJson(this, gsonType)
                gsonString
            }
            is List<*> -> {
                val gsonType = object : TypeToken<List<*>>() {}.type
                val gsonString: String = GsonHelper.gson.toJson(this, gsonType)
                gsonString
            }
            else -> {
                if (this is String) {
                    this
                } else {
                    this?.asString.toString()
                }
            }
        }

internal val Any?.truthy: Boolean
    get() = when (this) {
        null -> false
        is Boolean -> this
        is Number -> toDouble() != 0.0
        is String -> !isEmpty() && this != "[]" && this != "false" && this != "null"
        is Collection<*> -> !isEmpty()
        is Array<*> -> size > 0
        else -> true
    }

internal val List<Any?>.flat: List<Any?>
    get() = mutableListOf<Any?>().apply {
        this@flat.forEach {
            when (it) {
                is List<*> -> addAll(it.flat)
                else -> add(it)
            }
        }
    }

internal val Any?.asList: List<Any?>
    get() = when (this) {
        is String ->
            if (startsWith("["))
                replace("[", "").replace("]", "").split(",").map { it.trim() }
            else listOf(this)
        is List<*> -> this
        else -> listOf()
    }

internal val List<Any?>.comparableList: List<Comparable<*>?>
    get() = map { if (it is Comparable<*>) it else null }

internal val List<Any?>.doubleList: List<Double>
    get() = map {
        when (it) {
            is Number -> it.toDouble()
            is String -> it.doubleValue
            else -> 0.0
        }
    }

internal val String.noSpaces: String
    get() = replace(" ", "")

internal val String.intValue: Int
    get() = doubleValue.toInt()

internal val String.doubleValue: Double
    get() = toDouble()


internal val Any.asString: Any
    get() = when {
        this is String && startsWith("\"") && endsWith("\"") -> this
        this is String && toDoubleOrNull() != null && !contains(".") -> "\"$this\""
        this is String && toDoubleOrNull() != null -> this
        this is String -> "\"$this\""
        else -> this
    }

internal val String.unStringify: String
    get() = replace("\"", "")

internal fun getRecursive(indexes: List<String>, data: List<Any?>): Any? =
    indexes.getOrNull(0)?.apply {
        val d = data.getOrNull(intValue) as? List<Any?>
        return if (d is List<*>) getRecursive(
            indexes.subList(1, indexes.size),
            d
        ) else data.getOrNull(intValue)
    }
