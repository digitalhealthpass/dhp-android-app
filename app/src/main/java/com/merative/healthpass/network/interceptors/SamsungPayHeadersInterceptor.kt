package com.merative.healthpass.network.interceptors

import com.merative.healthpass.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class SamsungPayHeadersInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("partnerCode", BuildConfig.SPAY_SERVICEID)
            .build()
        return chain.proceed(request)
    }
}