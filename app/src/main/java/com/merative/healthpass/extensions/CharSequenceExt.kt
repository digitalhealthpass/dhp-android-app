package com.merative.healthpass.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern

fun CharSequence?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

private const val PATTERN_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
fun CharSequence.isColor(): Boolean {
    val colorPattern: Pattern = Pattern.compile(PATTERN_COLOR)
    val m: Matcher = colorPattern.matcher(this)
    return m.matches()
}

fun CharSequence.splitCamelCase(): String {

    return replace(
        String.format(
            "%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ).toRegex(),
        " "
    )
}

fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(this)
    }
}

fun String.removeExtraQuote(): String {
    return if (startsWith("\"") && endsWith("\"")) {
        replace("\"", "")
    } else {
        this
    }
}

fun CharSequence.getInitials(): String {
    return split(" ")
        .filterIndexed { index, s ->
            index == 0 || index == 1
        }
        .joinToString("") {
            it.firstOrNull().orValue("").toString()
        }
}

fun CharSequence?.orDash(): CharSequence = this ?: "-"