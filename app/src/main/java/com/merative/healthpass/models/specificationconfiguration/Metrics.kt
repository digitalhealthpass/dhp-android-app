package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Metrics(
    var name: String? = null,
    var extract: @RawValue JsonElement? = null,
    var countBy: CountBy? = CountBy(),
) : Parcelable