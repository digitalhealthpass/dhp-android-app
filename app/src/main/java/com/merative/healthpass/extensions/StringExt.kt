package com.merative.healthpass.extensions

fun String.isNumeric(): Boolean {
    val regex = """^(-)?[0-9]{0,}((\.){1}[0-9]{1,}){0,1}$""".toRegex()
    return if (this.isNullOrEmpty()) false
    else regex.matches(this)
}