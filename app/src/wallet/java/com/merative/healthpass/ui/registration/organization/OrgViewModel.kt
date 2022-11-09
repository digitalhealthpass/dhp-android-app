package com.merative.healthpass.ui.registration.organization

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.api.registration.hit.RegistrationResponse
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.network.serviceinterface.HitRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class OrgViewModel @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils,
    private val orgRegService: HitRegistrationService,
    private val nihRegRepo: NIHRegRepo
) : ViewModel() {

    fun requestOrgInfo(organizationName: String): Single<Response<RegistrationResponse>> {
        return orgRegService.requestOrganizationInfo(organizationName)
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            .asyncToUiSingle()
    }

    fun requestNih(organizationName: String): Single<Response<DisplaySchemaResponse>> {
        return nihRegRepo.displaySchema(organizationName)
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            .asyncToUiSingle()
    }
}