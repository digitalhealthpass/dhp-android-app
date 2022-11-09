package com.merative.watson.healthpass.verificationengine.rules

import android.util.Log
import com.google.gson.internal.LinkedTreeMap
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.payLoad
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.rules.jsonlogic.JSONLogic
import com.merative.watson.healthpass.verificationengine.rules.jsonlogic.unStringify
import java.text.ParseException
import java.util.*

fun VerifyEngine.doesMatchRules(rules: Map<String, Any>, valueSets: Map<String?, List<String?>>? = null): String {
    val payload = parse<Map<String, Any>>(verifiableObject?.payLoad.toString())
    val exp = ((payload as LinkedTreeMap<String, Any>)["exp"] as? Double)?.toLong()?.times(1000)
    val expDate = exp?.let { Date(it).format(SERVER_DATE_FORMAT, toUTCFormat = true) }

    val utcTime = Date().format(SERVER_DATE_FORMAT, toUTCFormat = true)
    val external = mutableMapOf<String, Any>(VerifyEngine.VALIDATION_CLOCK_STR to utcTime)

    valueSets?.forEach { (key, value) ->
        external[key.toString()] = value
    }

    //  Log.d("RULES", "---- expDate: $expDate; utcTime: $utcTime")
    val data = mapOf<String, Any>(
        VerifyEngine.PAYLOAD_STR to payload,
        VerifyEngine.EXTERNAL_STR to external
    )

    val jsonLogic = JSONLogic()
    jsonLogic.addOperation(VerifyEngine.PLUS_TIME_STR) { l, _ -> l.changeTime(isSubtraction = false) }
    jsonLogic.addOperation(VerifyEngine.MINUS_TIME_STR) { l, _ -> l.changeTime(isSubtraction = true) }
    jsonLogic.addOperation(VerifyEngine.IN_OPERATOR) { l, _ -> checkList(l) }
    jsonLogic.addOperation(VerifyEngine.LESS_THAN) { l, _ -> lessThan(l) }

    return jsonLogic.apply(rules, data, true).unStringify
}

fun VerifyEngine.doesMatchClassifierRules(rules: Map<String, Any>, valueSets: Map<String?, List<String?>>? = null): String {
    val payload = parse<Map<String, Any>>(verifiableObject?.payLoad.toString())
    val exp = ((payload as LinkedTreeMap<String, Any>)["exp"] as? Double)?.toLong()?.times(1000)
    val expDate = exp?.let { Date(it).format(SERVER_DATE_FORMAT, toUTCFormat = true) }

    val utcTime = Date().format(SERVER_DATE_FORMAT, toUTCFormat = true)
    val external = mutableMapOf<String, Any>(VerifyEngine.VALIDATION_CLOCK_STR to utcTime)

    valueSets?.forEach { (key, value) ->
        external[key.toString()] = value
    }

    //  Log.d("RULES", "---- expDate: $expDate; utcTime: $utcTime")
    val data = mapOf<String, Any>(
        VerifyEngine.PAYLOAD_STR to payload,
        VerifyEngine.EXTERNAL_STR to external
    )

    val jsonLogic = JSONLogic()
    jsonLogic.addOperation(VerifyEngine.PLUS_TIME_STR) { l, _ -> l.changeTime(isSubtraction = false) }
    jsonLogic.addOperation(VerifyEngine.MINUS_TIME_STR) { l, _ -> l.changeTime(isSubtraction = true) }
    jsonLogic.addOperation(VerifyEngine.IN_OPERATOR) { l, _ -> checkList(l) }
    jsonLogic.addOperation(VerifyEngine.LESS_THAN) { l, _ -> lessThan(l) }

    return jsonLogic.apply(rules, data, true).unStringify
}

fun VerifyEngine.checkList(l: List<Any?>?): Boolean {
    val first = l?.getOrNull(0).toString().unStringify
    val res = when (val second = l?.getOrNull(1)) {
        is String -> {
            val list = second.parseList()
            if (list.size > 1) list.firstOrNull { it == first } != null else second.contains(first)
        }
        is List<*> -> second.contains(first)
        else -> false
    }
    //Log.d(JSONLogic.TAG, "---- in: l:$l, res: $res")
    return res
}

fun VerifyEngine.lessThan(l: List<Any?>?): Boolean {
    var first = l?.getOrNull(0)
    var second = l?.getOrNull(1)

    if (first is String) {
        first = first.toFloatOrNull()
    }
    if (second is String) {
        second = second.toFloatOrNull()
    }

    if (first is Float && second is Float) {
        return first.toFloat() < second.toFloat()
    }
    return false
}

private fun String.parseList() = this.split(",").map {
    it.replace("\"", "")
        .replace("[", "")
        .replace("]", "")
}

fun isNumeric(toCheck: String): Boolean {
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return toCheck.matches(regex)
}

private fun List<Any?>?.changeTime(isSubtraction: Boolean): Long? {
    val dateString = this?.getOrNull(0)
    val amount = this?.getOrNull(1)
    val unit = this?.getOrNull(2)
    var parsedAmount = 0
    if (unit?.equals("day") == true || unit?.equals("hour") == true) {
        if (amount is Float) {
            parsedAmount = amount.toInt()
        } else if (amount is Double) {
            parsedAmount = amount.toInt()
        } else if (amount is Int) {
            parsedAmount = amount
        } else if (amount is String) {
            if (amount.toString().contains("\"")) {
                if (isNumeric(amount.toString().replace("\"", ""))) {
                    parsedAmount = amount.toString().replace("\"", "").toInt()
                }
            } else {
                if (isNumeric(amount.toString())) {
                    parsedAmount = amount.toString().toInt()
                }
            }
        }
    }
    if (dateString is String && unit is String) {
        val calendar =
            if (dateString == "now") {
                Calendar.getInstance()
            } else {
                dateString.unStringify.parseToCalendar() ?: return null
            }
        val timeShift = parsedAmount * (if (isSubtraction) -1 else 1)
        when (unit) {
            VerifyEngine.DAY_STR -> calendar?.add(Calendar.DAY_OF_MONTH, timeShift)
            VerifyEngine.HOUR_STR -> calendar?.add(Calendar.HOUR_OF_DAY, timeShift)
        }
        return calendar?.timeInMillis
    }
    return null
}

private fun String.parseToCalendar(): Calendar? {
    val dateFormatStr = when (this.length) {
        10 -> SERVER_SHORT_DATE_FORMAT
        20 -> SERVER_DATE_FORMAT
        else -> null
    }
    val calendar = Calendar.getInstance()
    try {
        calendar.time = if (dateFormatStr == null) {
            Date(this.toDouble().toLong().times(1000))
        } else {
            this.toDate(dateFormatStr, true)
        }
    } catch (ex: ParseException) {
        Log.w(ex.javaName(), "Error parse ${this.unStringify} to Date", ex)
        return null
    }
    return calendar
}

class RulesException : Exception("Credential rules did not match")