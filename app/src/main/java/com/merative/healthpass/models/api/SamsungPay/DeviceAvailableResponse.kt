package com.merative.healthpass.models.api.SamsungPay

import com.google.gson.annotations.SerializedName

data class DeviceAvailableResponse(

    @SerializedName("resultCode"    ) var resultCode    : String?  = null,
    @SerializedName("resultMessage" ) var resultMessage : String?  = null,
    @SerializedName("available"     ) var available     : Boolean? = null

)
