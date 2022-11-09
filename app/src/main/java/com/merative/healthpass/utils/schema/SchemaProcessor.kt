package com.merative.healthpass.utils.schema

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.schema.SchemaPayload
import com.merative.healthpass.models.sharedPref.Package
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.isIDHPorGHPorVC
import java.util.*
import kotlin.collections.ArrayList

class SchemaProcessor {

    fun processSchemaAndSubject(
        aPackage: Package,
        adjustHexColor: Boolean = true
    ): List<Field> {
        if (aPackage.schema == null) {
            loge("credential subject is null")
            aPackage.hexColor = AppConstants.COLOR_CREDENTIALS
            return emptyList()
        }

        val fieldsList = processSchemaAndSubject(
            aPackage.verifiableObject.credential!!,
            aPackage.schema
        )

        if (adjustHexColor) {
            aPackage.hexColor =
                fieldsList.firstOrNull { it.path.contains(Field.FIELD_NAME_DISPLAY) }?.colorFromEnums
                    .orValue(AppConstants.COLOR_CREDENTIALS)
        }

        fieldsList.forEach { it ->
            if (aPackage.verifiableObject.isIDHPorGHPorVC) {
                val (_, valueGName) = fieldsList.find { it.path == "recipient.givenName" || it.path == "subject.name.given" }
                    ?.getUsableValue(Locale.getDefault()) ?: "" to ""
                val (_, valueFName) = fieldsList.find { it.path == "recipient.familyName" || it.path == "subject.name.family" }
                    ?.getUsableValue(Locale.getDefault()) ?: "" to ""
                val (_, valueDOB) = fieldsList.find { it.path == "recipient.birthDate" || it.path == "subject.birthDate" }
                    ?.getUsableValue(Locale.getDefault()) ?: "" to ""
                when {
                    it.path.contains("given") -> {
                        it.value = valueGName.replace("\\n", "\n").replace("\\r","\n")
                    }
                    it.path.contains("family") -> {
                        it.value = valueFName.replace("\\n", "\n").replace("\\r","\n")
                    }
                    it.path.contains("birthDate") -> {
                        it.value = valueDOB
                    }
                }
            }
        }

        return fieldsList.map { updateDateFormat(it) }.map { it }
    }

    private fun updateDateFormat(field: Field): Field {
        if (field.value.toString().matches("\\d{4}-\\d{2}-\\d{2}.*".toRegex())) {
            field.valueAdditionalData = field.value.toString()
            field.value.toString().getLocaleFormattedDate(SERVER_SHORT_DATE_FORMAT)?.let {
                field.value = it
            }
        }
        return field
    }

     fun processSchemaAndSubject(
        credential: Credential?,
        schemaPayload: SchemaPayload?
    ): List<Field> {
        if (credential?.credentialSubject == null) {
            loge("credential subject is null")
            return emptyList()
        }

        return processSchemaAndSubject(
            credential.credentialSubject.orValue(JsonObject()),
            credential.obfuscation.asJsonArrayOrNull(),
            schemaPayload?.schema.orValue("{}").toJsonElement().asJsonObject
        )
    }

    private fun processSchemaAndSubject(
        subject: JsonObject,
        obfuscation: JsonElement?,
        schema: JsonObject
    ): List<Field> {
        logd("credentialSubject: $subject \n -------  -------  -------  -------  ------- ")
        logd("\n schema: $schema")

        val intermediateFields = processCredentialSubject(subject, "", obfuscation)
        return extractFieldsFromSchema(intermediateFields, schema)
    }

    private fun extractFieldsFromSchema(
        intermediateFields: List<Field>,
        schemaJSON: JsonObject,
    ): MutableList<Field> {
        val result: MutableList<Field> = LinkedList()
        var processedField: Field
        var paths: MutableList<String>

        for (field in intermediateFields) {
            paths = ArrayList<String>()
            val pathComponents = field.path.split("\\.".toRegex()).toTypedArray()
            for (pathComponent in pathComponents) {
                if (isSerialNumber(pathComponent)) {
                    continue
                }
                paths.add(pathComponent)
            }
            processedField = processSchema(schemaJSON, paths, field)
            if (!processedField.foundInSchema) {
                logw("WARNING, not found in schema: $processedField")
            }
            result.add(processedField)
        }

        return result
    }

    private fun processCredentialSubject(
        subject: JsonElement,
        path: String,
        obfuscation: JsonElement?
    ): List<Field> {
        var currentPath = path
        val result: MutableList<Field> = LinkedList()
        if (currentPath.isNotEmpty()) {
            currentPath += "."
        }
        if (subject is JsonObject) {
            val fields: Iterator<Map.Entry<String, JsonElement>> = subject.entrySet().iterator()
            var current: JsonElement
            var key: String
            while (fields.hasNext()) {
                val next = fields.next()
                current = next.value
                key = next.key
                if (current.isJsonObject) {
                    result.addAll(processCredentialSubject(current, currentPath + key, obfuscation))
                } else if (current.isJsonArray) {
                    val arrayNode = current as JsonArray

                    // represents values of type ["a", "b"]
                    if (arrayNode.size() == 0 || arrayNode[0].isJsonPrimitive) {
                        val obfuscatedObject = getObfuscation(currentPath + key, obfuscation)
                        result.add(Field(currentPath + key, current, obfuscatedObject))
                        continue
                    }
                    for (i in 0 until arrayNode.size()) {
                        val arrayElement = arrayNode[i]
                        val subPath = "$currentPath$key.$i"
                        result.addAll(processCredentialSubject(arrayElement, subPath, obfuscation))
                    }
                } else {
                    val obfuscatedObject = getObfuscation(currentPath + key, obfuscation)
                    result.add(
                        Field(
                            currentPath + key,
                            current.toString().removeExtraQuote(),
                            obfuscatedObject
                        )
                    )
                }
            }
        }
        return result
    }

    private fun getObfuscation(path: String, obfuscation: JsonElement?): JsonObject? {
        return obfuscation?.asJsonArray?.firstOrNull {
            it.asJsonObject.getStringOrNull("path").equals(path, true)
        }?.asJsonObject
    }

    private fun processSchema(schema: JsonObject, paths: List<String>, field: Field): Field {
        if (schema.has(Field.KEY_ITEMS)) {
            val searchField = processSchema(schema[Field.KEY_ITEMS] as JsonObject, paths, field)
            if (searchField.foundInSchema) {
                return searchField
            }
        }
        if (schema.has(Field.KEY_PROPERTIES)) {
            val propJsonObject = if (schema[Field.KEY_PROPERTIES].isJsonObject)
                schema[Field.KEY_PROPERTIES].asJsonObject
            else
                schema[Field.KEY_PROPERTIES].asString.toJsonElement().asJsonObject

            val searchField = processSchemaProperties(propJsonObject, paths, field)
            if (searchField.foundInSchema) {
                return searchField
            }
        }

        val candidatesToSearchIn = arrayOf("oneOf", "anyOf", "allOf", "not")
        for (candidate in candidatesToSearchIn) {
            if (schema.has(candidate) && schema[candidate].isJsonArray && (schema[candidate] as JsonArray)[0].isJsonObject) {
                val subSchemas = schema[candidate] as JsonArray
                for (i in 0 until subSchemas.size()) {
                    val searchField = processSchema(subSchemas[i] as JsonObject, paths, field)
                    if (searchField.foundInSchema) {
                        return searchField
                    }
                }
            }
        }
        return field
    }

    private fun processSchemaProperties(
        properties: JsonObject,
        paths: List<String>,
        field: Field
    ): Field {
        if (properties.has(paths[0])) {
            val schemaField = properties[paths[0]] as JsonObject
            return if (paths.size == 1) {
                parseFieldFromSchema(field, schemaField)
            } else {
                val newPaths = copyList(paths, 1)
                processSchema(schemaField, newPaths, field)
            }
        }
        return field
    }

    private fun parseFieldFromSchema(field: Field, fieldJson: JsonObject): Field {
        field.dataType = fieldJson.getStringOrNull(Field.KEY_TYPE).orValue("not-initialized")
        field.format = fieldJson.getStringOrNull(Field.KEY_FORMAT).orEmpty()
        field.description = fieldJson.getStringOrNull(Field.KEY_DESCRIPTION).orEmpty()
        field.pattern = fieldJson.getStringOrNull(Field.KEY_PATTERN).orEmpty()

        field.visible = fieldJson.getBooleanOrNull(Field.KEY_VISIBLE).orValue(true)

        field.enum = fieldJson.getListOrNull<Any>(Field.KEY_ENUM)
        if (field.enum == null && fieldJson.has(Field.KEY_ITEMS)) {
            field.enum =
                fieldJson.getAsJsonObject(Field.KEY_ITEMS).getListOrNull<Any>(Field.KEY_ENUM)
        }

        field.foundInSchema = true

        field.displayValueJson = fieldJson.getAsJsonObject(Field.KEY_DISPLAY_VALUE)
        field.displayValueArray = fieldJson.getAsJsonArray(Field.KEY_DISPLAY_VALUE_ARRAY)
        field.adjustDisplayValueIfNeeded()
        field.order = fieldJson.getIntOrNull(Field.KEY_ORDER).orValue(0)

        //this helps with views
        field.minLength = fieldJson.getStringOrNull(Field.KEY_MIN_LENGTH)?.toInt()
        field.maxLength = fieldJson.getStringOrNull(Field.KEY_MAX_LENGTH)?.toInt()
        field.displayType = fieldJson.getStringOrNull(Field.KEY_DISPLAY_TYPE).orEmpty()
        field.required = fieldJson.getBooleanOrDefault(Field.KEY_REQUIRED, false)

        return field
    }

    private fun copyList(source: List<String>, from: Int): List<String> {
        return ArrayList(source).subList(from, source.size)
    }

    private fun isSerialNumber(serial: String): Boolean {
        return serial.toLongOrNull(10) != null
    }

    fun processSchema(schemaPayload: SchemaPayload?): List<Field> {
        if (schemaPayload?.schema == null) return emptyList()

        val intermediateFields = schemaPayload.schema
            ?.required
            ?.map { Field(it, "", null) }
            .orEmpty()
            .toArrayList()

        val schemaJson = schemaPayload.schema.orValue("{}").toJsonElement().asJsonObject

        return extractFieldsFromSchema(intermediateFields, schemaJson)
    }

    fun processRegJson(jsonObject: JsonObject?): List<Field> {
        val result = ArrayList<Field>()
        jsonObject?.entrySet()?.forEach {
            val field = Field(it.key, "", null)
            parseFieldFromSchema(field, it.value.asJsonObject)
            result.add(field)
        }
        return result
    }
}