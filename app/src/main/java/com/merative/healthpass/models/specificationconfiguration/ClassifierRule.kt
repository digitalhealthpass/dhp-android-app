package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClassifierRule(
    var version: String? = null,
    var name: String? = null,
    var predicate: String? = null,
    var id: String? = null,
) : Parcelable