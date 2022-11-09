package com.merative.healthpass.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    @SerializedName("State")
    val state: String,
    @SerializedName("Abbrev")
    val abbrev: String,
    @SerializedName("Code")
    val code: String
) : Parcelable {

    //the [SelectionAdapter] is using toString function
    override fun toString(): String {
        return state
    }
}