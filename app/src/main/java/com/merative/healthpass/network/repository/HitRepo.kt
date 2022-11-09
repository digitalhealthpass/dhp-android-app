package com.merative.healthpass.network.repository

import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.hit.RegistrationBody
import com.merative.healthpass.network.serviceinterface.HitRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class HitRepo @Inject constructor(
    private val service: HitRegistrationService
) {
    fun registrationCode(
        organizationName: String,
        code: String
    ): Single<Response<BaseResponse>> {
        return service.registrationCode(
            code,
            RegistrationBody(organizationName)
        ).onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            .asyncToUiSingle()
    }
}