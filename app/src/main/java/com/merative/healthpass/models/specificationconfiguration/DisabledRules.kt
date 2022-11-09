package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisabledRules(
    var name: String? = null,
    var specID: String? = null,
    var id: String? = null,
) : Parcelable