package com.merative.healthpass.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Record(
    val data: String
) : Parcelable
