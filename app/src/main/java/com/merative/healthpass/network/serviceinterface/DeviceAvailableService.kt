package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.SamsungPay.DeviceAvailableResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DeviceAvailableService {

    @GET("/wallet/cmn/v2.0/device/available?")
    suspend fun getResult(
        @Query("countryCode") countryCode: String,
        @Query("modelName") modelName: String,
        @Query("serviceType") serviceType: String
    ): Response<DeviceAvailableResponse>

}