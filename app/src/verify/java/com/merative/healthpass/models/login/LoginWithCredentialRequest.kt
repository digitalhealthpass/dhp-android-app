package com.merative.healthpass.models.login

import com.google.gson.annotations.SerializedName

data class LoginWithCredentialRequest(
    @SerializedName("credential") val credential: String,
)