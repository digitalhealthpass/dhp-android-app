package com.merative.watson.healthpass.verifiablecredential.models.parcel

import android.os.Parcel
import com.google.gson.JsonElement
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import kotlinx.parcelize.Parceler

object JsonElementParceler : Parceler<JsonElement?> {

    override fun create(parcel: Parcel): JsonElement? {
        return parcel.readNullable { GsonHelper.gson.toJsonTree(parcel.readString()) }
    }

    override fun JsonElement?.write(parcel: Parcel, flags: Int) {
        parcel.writeNullable(this) { parcel.writeString(toString()) }
    }
}

inline fun <T> Parcel.readNullable(reader: () -> T) =
    if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: T.() -> Unit) {
    if (value != null) {
        writeInt(1)
        value.writer()
    } else {
        writeInt(0)
    }
}