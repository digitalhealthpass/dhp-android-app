package com.merative.healthpass.models

data class User(
    val email: String?,
    val displayName: String?,
    val loginTime: Long,
    val accessToken: String,
    /**This hold time the access token valid for, for it is 30 minutes, but it can changes*/
    val exp: Long?
)
