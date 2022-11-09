package com.merative.healthpass.network.repository

import com.merative.healthpass.exception.ExpirationException
import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.stringfyAndEncodeBase64
import com.merative.healthpass.models.api.login.LoginResponse
import com.merative.healthpass.models.login.LoginWithCredentialRequest
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.interceptors.LoginInterceptor
import com.merative.healthpass.network.serviceinterface.LoginService
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class LoginRepo @Inject constructor(
    private val loginService: LoginService,
    private val sharedPrefUtils: SharedPrefUtils,
) {

    fun loginWithCredential(aPackage: Package): Single<Response<LoginResponse>> {
        return Single.just(aPackage)
            .map { isCredentialValid(aPackage) }
            .flatMap { valid -> loginWithCredential(valid, aPackage) }
            .flatMap { response -> handleResponse(response) }
            .asyncToUiSingle()
    }

    private fun isCredentialValid(aPackage: Package): Boolean {
        val credential = aPackage.verifiableObject.credential!!
        return !credential.isExpired() && credential.isValid()
    }

    private fun loginWithCredential(
        valid: Boolean,
        aPackage: Package
    ): Single<Response<LoginResponse>> {
        return if (valid) {
            val encoded = aPackage.verifiableObject.credential!!.stringfyAndEncodeBase64()
            val body = LoginWithCredentialRequest(encoded)
            loginService.loginWithCredential(body)
        } else {
            throw ExpirationException("Expired credential")
        }
    }

    private fun handleResponse(response: Response<LoginResponse>): Single<Response<LoginResponse>> {
        return if (response.isSuccessfulAndHasBody()) {
            val user = LoginInterceptor.parseLoginResponse(response.body()!!)
            sharedPrefUtils.saveUser(user)
            Single.just(response)
        } else {
            throw HttpException(response)
        }
    }
}
