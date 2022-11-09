package com.merative.healthpass.network.interceptors

import android.util.Log
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.loge
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.extensions.stringfyAndEncodeBase64
import com.merative.healthpass.models.User
import com.merative.healthpass.models.api.login.LoginResponse
import com.merative.healthpass.models.login.LoginWithCredentialRequest
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.JWTUtils
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.watson.healthpass.verifiablecredential.extensions.javaName
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import io.reactivex.rxjava3.core.Observable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class LoginInterceptor @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
    private val environmentHandler: EnvironmentHandler,
    private val packageDB: PackageDB,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val defaultUrl =
            chain.request().url.toString().startsWith(environmentHandler.currentEnv.url)

        if (defaultUrl) {
            loginIfNeeded()
        }

        return chain.proceed(chain.request())
    }

    private fun loginIfNeeded() {
        val loadedUser = sharedPrefUtils.getUser()
        val loginTime = loadedUser?.loginTime.orValue(0L)

        val currentTime = Calendar.getInstance().timeInMillis
        val expTime = loginTime.plus(AppConstants.ACCESS_TOKEN_VALID_TIME)
        val list = packageDB.loadAll().blockingGet().dataList

        if ((loginTime == 0L || currentTime > expTime) && list.size > 0) {

            val credential = list[list.size - 1].verifiableObject.credential!!.stringfyAndEncodeBase64()

            val body: RequestBody =
                LoginWithCredentialRequest(credential).stringfy()
                    .toRequestBody("application/json".toMediaType())

            val request: Request = Request.Builder()
                .addHeader("x-hpass-issuer-id", environmentHandler.currentEnv.issuerId)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("x-hpass-txn-id", HeadersInterceptor.createUUID())
                .url("${environmentHandler.currentEnv.url}hpass/users/login/loginWithCredential")
                .post(body)
                .build()

            val client = OkHttpClient.Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        val interceptor = HttpLoggingInterceptor()
                        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                        addInterceptor(interceptor)
                    }
                }
                .build()

            Observable
                .fromCallable { client.newCall(request).execute() }
                .blockingSubscribe({ response ->
                    if (response.isSuccessful && response.body != null) {
                        val loginResponse = parse<LoginResponse>(response.body!!.string())
                        val responseUser = parseLoginResponse(loginResponse)
                        sharedPrefUtils.saveUser(responseUser)
                    }
                }, { loge("failed to send login request", it, true) })
        }
    }

    companion object {
        fun parseLoginResponse(response: LoginResponse): User {
            val decodedResponseBody = JWTUtils.decoded(response.accessToken!!)

            val exp = decodedResponseBody.toJSONObject()?.getLong("exp")
            val responseLoginTime = Calendar.getInstance().timeInMillis

            return User(
                null,
                null,
                responseLoginTime,
                response.accessToken.toString(),
                exp
            )
        }

        /**
         * convert to [JSONObject] or null if it fails
         */
        private fun String.toJSONObject(): JSONObject? {
            return try {
                JSONObject(this)
            } catch (ex: JSONException) {
                Log.e(javaName(), "failed to convert to JSONObject", ex)
                null
            }
        }
    }
}