package com.merative.healthpass.network.interceptors

import android.util.Log
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.logd
import com.merative.healthpass.extensions.loge
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.extensions.randomObject
import com.merative.healthpass.models.User
import com.merative.healthpass.models.api.login.LoginRequest
import com.merative.healthpass.models.api.login.LoginResponse
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.JWTUtils
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
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        loginIfNeeded()

        return chain.proceed(chain.request())
    }

    private fun loginIfNeeded() {
        val loadedUser = sharedPrefUtils.getUser()
        val loginTime = loadedUser?.loginTime.orValue(0L)

        val currentTime = Calendar.getInstance().timeInMillis
        val expTime = loginTime.plus(AppConstants.ACCESS_TOKEN_VALID_TIME)

        if (loginTime == 0L || currentTime > expTime) {
            val (userName, password) = getUserPass()
            val loginRequest = LoginRequest(userName, password)

            val body: RequestBody =
                loginRequest.stringfy().toRequestBody("application/json".toMediaType())

            val request: Request = Request.Builder()
                .addHeader("x-hpass-issuer-id", environmentHandler.currentEnv.issuerId)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("x-hpass-txn-id", HeadersInterceptor.createUUID())
                .url("${environmentHandler.currentEnv.url}hpass/users/login/")
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
            val decodedResponseBody = JWTUtils.decoded(response.idToken!!)

            val email = getStringFromResponse("email", decodedResponseBody)
            val name = getStringFromResponse("name", decodedResponseBody)
            val exp = decodedResponseBody.toJSONObject()?.getLong("exp")
            val responseLoginTime = Calendar.getInstance().timeInMillis

            return User(
                email,
                name,
                responseLoginTime,
                response.accessToken.toString(),
                exp
            )
        }

        private fun getStringFromResponse(param: String, body: String): String {
            return try {
                val jsonObject = JSONObject(body)
                jsonObject.getString(param)
            } catch (e: Exception) {
                logd("failed to get string", e)
                throw JSONException("Invalid json")
            }
        }

        fun getUserPass(): Pair<String, String> {
            return randomObject(
                "tester@poc.com" to "testing123"
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