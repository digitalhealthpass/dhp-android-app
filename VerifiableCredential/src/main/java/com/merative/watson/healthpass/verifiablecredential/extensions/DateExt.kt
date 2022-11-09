package com.merative.watson.healthpass.verifiablecredential.extensions

import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

const val SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
const val SERVER_SHORT_DATE_FORMAT = "yyyy-MM-dd"

fun Date.format(
    dateFormat: Int = DateFormat.LONG,
    locale: Locale = Locale.getDefault(),
    toUTCFormat: Boolean = false
): String {
    return DateFormat.getDateInstance(dateFormat, locale)
        .apply {
            if (toUTCFormat)
                timeZone = TimeZone.getTimeZone("UTC")
        }.format(this)
}

//For using String Constants defined dateFormat
fun Date.format(
    dateFormat: String,
    locale: Locale = Locale.getDefault(),
    toUTCFormat: Boolean = false
): String {
    return SimpleDateFormat(dateFormat, locale)
        .apply {
            if (toUTCFormat)
                timeZone = TimeZone.getTimeZone("UTC")
        }.format(this)
}

fun Date.localeFormat(
    dateFormat: Int = DateFormat.LONG,
    locale: Locale = Locale.getDefault(),
    toUTCFormat: Boolean = false
): String {
    return DateFormat.getDateInstance(dateFormat, locale)
        .apply {
            if (toUTCFormat)
                timeZone = TimeZone.getTimeZone("UTC")
        }.format(this)
}

fun String.getFormattedDate(
    dateFormat: String,
    outputFormat: Int,
    locale: Locale = Locale.getDefault(),
    toUTCFormat: Boolean = false,
): String? {
    return toDate(dateFormat, toUTCFormat)?.format(outputFormat, locale, toUTCFormat)
}

fun String.getLocaleFormattedDate(
    dateFormat: String,
    outputFormat: Int = DateFormat.SHORT,
    locale: Locale = Locale.getDefault(),
    toUTCFormat: Boolean = false,
): String? {
    return toDate(dateFormat, toUTCFormat)?.localeFormat(outputFormat, locale, toUTCFormat)
}

fun Long.timestampToDate(): Date? {
    return try {
        Date(this)
    } catch (e: Exception) {
        Log.e("TAG", "timestampToUtcDate: $e")
        return null
    }
}

fun String.toDate(
    dateFormat: String,
    toUTCFormat: Boolean,
): Date? {
    return try {
        SimpleDateFormat(dateFormat, Locale.getDefault())
            .apply {
                if (toUTCFormat)
                    timeZone = TimeZone.getTimeZone("UTC")
            }.parse(this)

    } catch (e: ParseException) {
        Log.e("Date Ext", "failed to parse date", e)
        null
    }
}

fun Date.getCurrentDateInUtcFormat(): Date {
    return Date(Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis)
}