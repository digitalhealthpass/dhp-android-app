package com.merative.watson.healthpass.verifiablecredential.models.jws

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

enum class CompressionAlgorithm(val value: String) {
    @Keep
    @SerializedName("DEF")
    DEFLATE("DEF")
}