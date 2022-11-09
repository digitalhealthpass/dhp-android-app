package com.merative.healthpass.models.api.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("id_token") val idToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: String?,
    @SerializedName("scope") val scope: String?
)