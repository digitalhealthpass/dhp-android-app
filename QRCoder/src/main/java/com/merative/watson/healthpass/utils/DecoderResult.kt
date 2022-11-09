package com.merative.watson.healthpass.utils

data class DecoderResult<T>(
    var result: String,
    val source: T? = null
)
