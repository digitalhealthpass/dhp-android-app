package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.login.LoginResponse
import com.merative.healthpass.models.login.LoginWithCredentialRequest
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST("hpass/users/loginWithCredential")
    fun loginWithCredential(
        @Body body: LoginWithCredentialRequest,
    ): Single<Response<LoginResponse>>
}