package com.merative.watson.healthpass.verificationengine.models.issuer

sealed class JWKError : Exception() {
    object MissingXComponent : JWKError()
    object MissingYComponent : JWKError()
    data class IncorrectKeyType(val expected: String, val actual: String) : JWKError()
    data class UnsupportedCurve(val crv: String?) : JWKError()
    data class KeyWithIDNotFound(val expected: String) : JWKError()
}