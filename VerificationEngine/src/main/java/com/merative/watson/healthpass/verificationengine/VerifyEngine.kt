package com.merative.watson.healthpass.verificationengine

import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.models.issuer.Issuer
import com.merative.watson.healthpass.verificationengine.models.issuer.IssuerKey
import com.merative.watson.healthpass.verificationengine.models.issuer.JWK
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class VerifyEngine {
    var verifiableObject: VerifiableObject? = null

    var issuer: Issuer? = null
    var jwkSet: List<JWK>? = null
    var issuerKeys: List<IssuerKey>? = null

    constructor()

    constructor(
        verifiableObject: VerifiableObject,
        issuer: Issuer? = null,
        jwkSet: List<JWK>? = null,
        issuerKeys: List<IssuerKey>? = null
    ) {
        this.verifiableObject = verifiableObject
        this.issuer = issuer
        this.jwkSet = jwkSet
        this.issuerKeys = issuerKeys
    }

    init {
        Security.addProvider(BouncyCastleProvider()) // for SHA256withRSA/PSS
    }

    companion object {

        const val TAG = "VerifyEngine"

        const val EC_NAME = "secp256r1"
        const val VALID_CRV = "P-256"

        const val TIMEZONE = "gmt"
        const val VALIDATION_CLOCK_STR = "validationClock"
        const val PAYLOAD_STR = "payload"
        const val EXTERNAL_STR = "external"
        const val PLUS_TIME_STR = "plusTime"
        const val MINUS_TIME_STR = "minusTime"
        const val IN_OPERATOR = "in"
        const val DAY_STR = "day"
        const val HOUR_STR = "hour"
        const val LESS_THAN = "lessThan"

        const val RULES_MATCH_TRUE = "true"
        const val RULES_MATCH_FALSE = "false"
        const val RULES_MATCH_UNKNOWN = "unknown"

    }
}