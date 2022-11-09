package com.merative.watson.healthpass.verifiablecredential.models.jws

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

enum class SignatureAlgorithm(val value: String) {
    @Keep
    @SerializedName("ES256")
    ES256("ES256")
}