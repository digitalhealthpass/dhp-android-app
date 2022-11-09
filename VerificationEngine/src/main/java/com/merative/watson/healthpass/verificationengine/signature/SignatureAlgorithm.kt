package com.merative.watson.healthpass.verificationengine.signature

enum class SignatureAlgorithm(val value: String) {
    ECDSA256("SHA256withECDSA"),
    RSA256_PSS("SHA256withRSA/PSS")
}