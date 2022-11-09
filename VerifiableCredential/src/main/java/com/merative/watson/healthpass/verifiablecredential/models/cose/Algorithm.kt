package com.merative.watson.healthpass.verifiablecredential.models.cose

import com.upokecenter.cbor.CBORObject

enum class Algorithm(val value: Int) {
    ES_256(-7),//ios 6
    PS_256(-37);//ios 36

    fun asCBOR(): CBORObject {
        return CBORObject.FromObject(value)
    }
}