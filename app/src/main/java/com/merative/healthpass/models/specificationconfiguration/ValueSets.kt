package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ValueSets(
    var version: String? = null,
    var name: String? = null,
    var description: String? = null,
    var items: ArrayList<Items> = arrayListOf(),
    var id: String? = null,
) : Parcelable