package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.hit.*
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.*

interface HitRegistrationService {
    @GET("datasubmission/organization/{org}/regconfig")
    fun requestOrganizationInfo(
        @Path("org") orgPath: String,
    ): Single<Response<RegistrationResponse>>

    @POST("datasubmission/onboarding/mfa/registration-code/{code}")
    fun registrationCode(
        @Path("code") code: String,
        @Body body: RegistrationBody,
        @Header("x-hpass-send-sms") contentRange: Boolean = true
    ): Single<Response<BaseResponse>>

    @POST("datasubmission/onboarding/mfa/verification-code/{code}")
    fun verifyCode(
        @Path("code") code: String,
        @Body body: RegistrationBody,
    ): Single<Response<VerificationCodeResponse>>

    @POST("datasubmission/onboarding/mfa/submit-registration")
    fun submitRegistration(
        @Body body: RegistrationSubmitBody,
    ): Single<Response<RegSubmitResponse>>
}