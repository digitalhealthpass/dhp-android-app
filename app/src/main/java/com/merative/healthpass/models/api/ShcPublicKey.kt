package com.merative.healthpass.models.api

import com.merative.watson.healthpass.verificationengine.models.issuer.JWK

data class ShcPublicKey(
    var name: String?,
    var url: String?,
    var keys: List<JWK>?,
)