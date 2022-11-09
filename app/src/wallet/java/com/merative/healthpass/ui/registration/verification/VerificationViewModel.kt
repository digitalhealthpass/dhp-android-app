package com.merative.healthpass.ui.registration.verification

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.api.registration.hit.RegistrationBody
import com.merative.healthpass.models.api.registration.hit.VerificationCodeResponse
import com.merative.healthpass.network.serviceinterface.HitRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class VerificationViewModel @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils,
    private val hitRegService: HitRegistrationService
) : ViewModel() {

    fun requestInfo(
        organizationName: String,
        code: String
    ): Single<Response<VerificationCodeResponse>> {
        return hitRegService.verifyCode(
            code,
            RegistrationBody(organizationName)
        )
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            .asyncToUiSingle()
    }
}