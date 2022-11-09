package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountBy(
    @SerializedName("scan") var scan: Boolean? = null,
    @SerializedName("scanResult") var scanResult: Boolean? = null,
    @SerializedName("extract") var extract: Boolean? = null,
) : Parcelable