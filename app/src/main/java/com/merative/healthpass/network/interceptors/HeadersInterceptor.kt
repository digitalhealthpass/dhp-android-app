package com.merative.healthpass.network.interceptors

import androidx.annotation.CheckResult
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.models.api.CircularQueue
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.SharedPrefUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*
import javax.inject.Inject

class HeadersInterceptor @Inject constructor(
    private val environmentHandler: EnvironmentHandler,
    private val sharedPrefUtils: SharedPrefUtils
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .addHeader("x-hpass-issuer-id", environmentHandler.currentEnv.issuerId)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("x-hpass-txn-id", createUUID())

        val defaultUrl =
            chain.request().url.toString().startsWith(environmentHandler.currentEnv.url)
        val user = sharedPrefUtils.getUser()
        if (user != null && defaultUrl) {
            requestBuilder
//                .removeHeader("Authorization")//remove old Authorization header since it has an invalid value
                .addHeader("Authorization", "Bearer ${user.accessToken}")
        }

        return chain.proceed(requestBuilder.build())
    }

    companion object {
        const val MAX_SIZE = 7
        private val uuidList = CircularQueue<String>(MAX_SIZE)

        @CheckResult
        fun createUUID(): String {
            val utcTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000
            val random = randomNumber()
            val extra = "${BuildConfig.VERSION_CODE}-3" //this represents Android

            val uuid = "${utcTime}-${random}-$extra"
            uuidList.add(uuid)

            return uuid
        }

        fun getLast5UUID(): CharSequence = uuidList.joinToString("\n")

        private fun randomNumber(min: Int = 100, max: Int = 999): Int {
            return Random().nextInt(max - min + 1) + min
        }
    }
}