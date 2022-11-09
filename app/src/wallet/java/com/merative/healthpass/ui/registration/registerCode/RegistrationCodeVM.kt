package com.merative.healthpass.ui.registration.registerCode

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.network.repository.HitRepo
import com.merative.healthpass.network.repository.NCLRegRepo
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class RegistrationCodeVM @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils,
    private val hitRepo: HitRepo,
    private val nihRegRepo: NIHRegRepo,
    private val nclRegRepo: NCLRegRepo
) : ViewModel() {

    fun requestHit(organizationName: String, code: String): Single<Response<BaseResponse>> {
        return hitRepo.registrationCode(organizationName, code)
    }

    fun validateNihCode(orgName: String, code: String): Single<Response<BaseResponse>> {
        return nihRegRepo.validateCode(orgName, code)
    }

    fun validateNclCode(orgName: String, code: String): Single<Response<BaseResponse>> {
        return nclRegRepo.validateCode(orgName, code)
    }
}