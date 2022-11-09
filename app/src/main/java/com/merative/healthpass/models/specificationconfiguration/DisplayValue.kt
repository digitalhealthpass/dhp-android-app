package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisplayValue(
    @SerializedName("en") var en: String? = null,
    @SerializedName("de") var de: String? = null,
) : Parcelable