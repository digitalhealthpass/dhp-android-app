package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Display(
    var version: String? = null,
    var name: String? = null,
    var fields: ArrayList<Fields> = arrayListOf(),
    var id: String? = null,
) : Parcelable