package com.merative.watson.healthpass.verifiablecredential.models.cose

import com.upokecenter.cbor.CBORObject

enum class CoseType(val tag: String, val value: String) {
    SIGNATURE_1("18", "Signature1"),
    SIGNATURE("98", "Signature");

    fun asCBOR(): CBORObject {
        return CBORObject.FromObject(tag)
    }
}