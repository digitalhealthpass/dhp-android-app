package com.merative.watson.healthpass.verifiablecredential.models.cose

import com.upokecenter.cbor.CBORObject


enum class CoseHeaderKeys(val value: Int, val keyName: String) {
    ALGORITHM(1, "alg"),
    CRITICAL_HEADERS(2, "crit"),
    CONTENT_TYPE(3, "content type"),
    KID(4, "kid"),
    IV(5, "IV"),
    PARTIAL_IV(6, "Partial IV"),
    TRUSTLIST_VERSION(42, "tlv");

    fun asCBOR(): CBORObject {
        return CBORObject.FromObject(value)
    }
}