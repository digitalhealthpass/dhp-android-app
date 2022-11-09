package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@JvmSuppressWildcards
interface NclRegistrationService {

    @GET("datasubmission/onboarding/{orgName}/validatecode/{code}")
    fun validateCode(
        @Path("orgName") orgName: String,
        @Path("code") code: String,
    ): Single<Response<BaseResponse>>

    @POST("datasubmission/onboarding/")
    fun onBoarding(
        @Body body: RequestBody
    ): Single<Response<OnBoardingResponse>>
}