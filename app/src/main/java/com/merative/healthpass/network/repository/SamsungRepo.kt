package com.merative.healthpass.network.repository

import android.content.ContentValues
import android.util.Log
import com.merative.healthpass.network.serviceinterface.DeviceAvailableService
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import javax.inject.Inject

class SamsungRepo @Inject constructor(
    private val service: DeviceAvailableService
) {
    suspend fun checkDeviceAvailSamsungPay(countryCode: String, modelName: String, serviceType: String): Boolean {
        var available = false

        val response = service.getResult(countryCode, modelName, serviceType)
        Log.d(ContentValues.TAG, "URL/Request: " + response.raw().request.url)
        Log.d(ContentValues.TAG, "Response: " + response.body()?.toJsonElement().toString())
        if (response.isSuccessful) {
            available = response.body()?.available!!
        }
        return available
    }
}