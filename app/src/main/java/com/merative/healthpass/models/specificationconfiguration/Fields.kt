package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fields(
    @SerializedName("field") var field: String? = null,
    @SerializedName("displayValue") var displayValue: DisplayValue? = DisplayValue(),
) : Parcelable