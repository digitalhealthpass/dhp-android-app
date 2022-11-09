package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Items(
    var value: String? = null,
    var description: String? = null
) : Parcelable