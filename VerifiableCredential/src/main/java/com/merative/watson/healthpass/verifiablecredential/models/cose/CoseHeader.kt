package com.merative.watson.healthpass.verifiablecredential.models.cose

import com.upokecenter.cbor.CBORObject

data class CoseHeader(
    val rawHeader: CBORObject,
    val keyId: CBORObject?,
    val algorithm: Algorithm?,
)