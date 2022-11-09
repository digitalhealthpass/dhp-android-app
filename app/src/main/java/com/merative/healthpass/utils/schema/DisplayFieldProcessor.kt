package com.merative.healthpass.utils.schema

import android.util.Log
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.credential.DisplayField
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import java.util.*

class DisplayFieldProcessor(val fieldJson: String) {

    fun getDisplayFields(
        verifiableObject: VerifiableObject,
        valueMapper: Map<String, Map<String, String>>
    ): List<Field> {
        return getDisplayFields(verifiableObject).map { field ->
            if (field.valueMapper != null) {
                valueMapper[field.valueMapper]
                    ?.get((field.value as String))
                    ?.let { field.value = it }
            }
            field
        }
    }

    fun getDisplayFields(verifiableObject: VerifiableObject): List<Field> {
        val type = verifiableObject.type
        val displayJsonArray: List<DisplayField> = parse(fieldJson)
        val fieldList = mutableListOf<Field>()

        if (displayJsonArray.isNotEmpty() && !verifiableObject.isRulesUnknown) {
            displayJsonArray.forEach { displayField ->

                val path = displayField.field
                    .replace("[", ".")
                    .replace("]", "")
                    .split(".")
                    .toArrayList()

                when (type) {
                    VCType.IDHP, VCType.GHP, VCType.VC -> {
                        val credentialSubject = verifiableObject.credential?.stringfy()
                            ?: throw IllegalStateException("IDHP or GHP must not be null")
                        val map: Map<String, Any> = parse(credentialSubject)

                        processPath(path, map, displayField, fieldList)
                    }
                    VCType.SHC -> {
                        val credentialSubject = verifiableObject.jws?.payload?.stringfy()
                            ?: throw IllegalStateException("SHC must not be null")
                        val map: Map<String, Any> = parse(credentialSubject)

                        processPath(path, map, displayField, fieldList)
                    }
                    VCType.DCC -> {
                        val credentialSubject = verifiableObject.cose?.hCertJson
                            ?: throw IllegalStateException("DCC must not be null")
                        val map: Map<String, Any> = parse(credentialSubject)

                        processPath(path, map, displayField, fieldList)
                    }
                    else -> {
                        val credentialSubject =
                            verifiableObject.credential?.credentialSubject?.stringfy()
                                ?: throw IllegalStateException("Credential subject must not be null")
                        val map: Map<String, Any> = parse(credentialSubject)

                        processPath(path, map, displayField, fieldList)
                        Log.d(VerifyEngine.TAG, "Unknown IDHP, GHP or VC type")
                    }
                }
            }
        } else {
            Log.d(VerifyEngine.TAG, "Display set empty.")
            val credentialSubject =
                verifiableObject.credential?.credentialSubject?.stringfy()
                    ?: throw IllegalStateException("Credential subject must not be null")

            val fieldMap: Map<String, Any> = parse(credentialSubject)
            getAllCredentialSubjectFields(fieldMap, fieldList)
        }
        return fieldList.map { updateDateFormat(it) }.toList()
    }

    private fun getAllCredentialSubjectFields(
        fieldMap: Map<String, Any>,
        fieldList: MutableList<Field>
    ) {
        fieldMap.forEach {
            if (it.value.toString().startsWith("[")) {
                val list: List<Map<String, Any>> = parse(it.value.stringfy())
                list.forEach { map -> getAllCredentialSubjectFields(map, fieldList) }
            } else if (it.value.toString().contains("{")) {
                val subFieldMap: Map<String, Any> = parse(it.value.stringfy())
                getAllCredentialSubjectFields(subFieldMap, fieldList)
            } else {
                fieldList.add(Field.create(it.key, it.value.toString().capitalize(Locale.ROOT)))
            }
        }
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

    private fun processPath(
        path: MutableList<String>,
        map: Map<String, Any>,
        displayField: DisplayField,
        fieldList: MutableList<Field>
    ) {
        var current: String? = null
        path.forEach { name ->
            if (current == name) return@forEach

            when (val value = map[name]) {
                null -> {
                    return
                }
                is List<*> -> {
                    current = name
                    val tempList = value as List<Any>
                    val pathUpd = getUpdatedPath(path, name)

                    if (pathUpd.size > 0) {
                        val tempMap = createMapFromList(tempList)
                        processPath(pathUpd, tempMap, displayField, fieldList)
                    } else {
                        val listString = value.toString()
                        createNewField(
                            displayField,
                            listString.substring(1, listString.length - 1),
                            fieldList
                        )
                        return@forEach
                    }
                }
                is Map<*, *> -> {
                    current = name
                    val tempMap: Map<String, Any> = value as Map<String, Any>
                    val pathUpd = getUpdatedPath(path, name)

                    processPath(pathUpd, tempMap, displayField, fieldList)
                }
                else -> {
                    current = name
                    createNewField(displayField, value, fieldList)
                    return@forEach
                }
            }
        }
    }

    private fun createMapFromList(tempList: List<Any>): MutableMap<String, Any> {
        val tempMap = mutableMapOf<String, Any>()
        tempList.forEachIndexed { index, value ->
            tempMap[index.toString()] = value
        }
        return tempMap
    }

    private fun getUpdatedPath(path: MutableList<String>, name: String): MutableList<String> {
        val pathUpd = path.toMutableList()
        pathUpd.remove(name)
        return pathUpd
    }

    private fun createNewField(
        displayField: DisplayField,
        item: Any?,
        fieldList: MutableList<Field>
    ) {
        val value = when (item) {
            is String -> item
            is Double -> item.toInt().toString()
            else -> item.toString()
        }
        val field = Field(displayField.field, value, null)
        field.displayValueJson = displayField.displayValue
        field.sectionIndex = displayField.sectionIndex
        field.sectionTitle = displayField.sectionTitle
        field.valueMapper = displayField.valueMapper
        if (!fieldList.contains(field)) {
            fieldList.add(field)
        }
    }
}