package com.merative.watson.healthpass.verifiablecredential.extensions

import com.upokecenter.cbor.CBORObject
import java.lang.IllegalStateException

fun CBORObject.getOrNull(index: Int): CBORObject? {
    return try {
        get(index)
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        null
    }
}

fun CBORObject.getInt64orDoubleAsInt64(): Long {
    return try {
        this.AsInt64Value()
    } catch (ise: IllegalStateException) {
        ise.printStackTrace()
        this.AsDouble().toLong()
    }
}