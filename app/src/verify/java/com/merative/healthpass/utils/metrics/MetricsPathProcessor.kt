package com.merative.healthpass.utils.metrics

import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.metrics.MetricsPayload
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import org.json.JSONObject

class MetricsPathProcessor(
    val verifiableObject: VerifiableObject,
    val metricsPayloadList: List<MetricsPayload>
) {

    private var credentialJson: JSONObject? = null
    private var payloadMap: Map<String, Any>? = null

    init {
        this.credentialJson = getCredentialJson()
        println(credentialJson)
        this.payloadMap = parse(this.credentialJson.toString())
    }

    private fun getCredentialJson() =
        when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> verifiableObject.credential?.stringfy()
            VCType.SHC -> verifiableObject.jws?.payload?.stringfy()
            VCType.DCC -> verifiableObject.cose?.hCertJson
            else -> null
        }?.toJSONObject()


    fun getMetricsFieldByKey(key: String): String? {
        val fields = metricsPayloadList.map { it.extract?.asJsonObject?.getStringOrNull(key) }
        val result = fields.map { field ->
            val path = getModifiedPath(field)

            processPath(path, payloadMap!!)
        }
        logd("-------- fields: $fields | key: $key | result: $result")
        return if (result.isEmpty()) null else result[METRICS_INDEX]
    }

    private fun getModifiedPath(field: String?) = field
        ?.replace("[", ".")
        ?.replace("]", "")
        ?.replace("..", ".")
        ?.split(".")
        ?.toMutableList() ?: mutableListOf()

    private fun processPath(
        path: MutableList<String>,
        map: Map<String, Any>
    ): String? {
        var result: String? = null
        var current: String? = null
        path.forEach { name ->
            if (name == "*" || name == "$") {
                map.keys.map { key ->
                    if (result != null) return@forEach
                    getResult(map, path, key, current)?.let { result = it }.also { current = key }
                }
            } else {
                getResult(map, path, name, current)?.let { result = it }.also { current = name }
            }
        }
        return result
    }

    fun getResult(
        map: Map<String, Any>,
        path: MutableList<String>,
        name: String,
        current: String?
    ): String? {
        if (current == name) return null
        return when (val value = map[name]) {
            null -> null
            is List<*> -> processList(name, value, path)
            is Map<*, *> -> processMap(name, value, path)
            else -> value.toString()
        }
    }

    private fun processMap(name: String, value: Any?, path: MutableList<String>): String? {
        val tempMap: Map<String, Any> = value as Map<String, Any>
        val pathUpd = getUpdatedPath(path, name)

        return processPath(pathUpd, tempMap)
    }

    private fun processList(key: String, value: Any?, path: MutableList<String>): String? {
        val tempList = value as List<Any>
        val pathUpd = getUpdatedPath(path, key)
        val tempMap = createMapFromList(tempList)

        return processPath(pathUpd, tempMap)
    }

    private fun getUpdatedPath(path: MutableList<String>, name: String): MutableList<String> {
        val pathUpd = path.toMutableList()
        pathUpd.remove(name)
        return pathUpd
    }

    private fun createMapFromList(tempList: List<Any>): MutableMap<String, Any> {
        val tempMap = mutableMapOf<String, Any>()
        tempList.forEachIndexed { index, value ->
            tempMap[index.toString()] = value
        }
        return tempMap
    }

    companion object {
        private const val METRICS_INDEX = 0
    }
}