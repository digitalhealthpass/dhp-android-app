package com.merative.healthpass.ui.registration.userAgreement

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.api.registration.hit.RegSubmitResponse
import com.merative.healthpass.models.api.registration.hit.RegistrationSubmitBody
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.network.repository.NCLRegRepo
import com.merative.healthpass.network.serviceinterface.HitRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.healthpass.utils.schema.Field
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class UserAgreementVM @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils,
    private val registrationService: HitRegistrationService,
    private val nclRegRepo: NCLRegRepo,
) : ViewModel() {
    val asymmetricKey by lazy { AsymmetricKey.createKeyTest() }

    val fieldsMap = HashMap<Field, Any?>()
    fun submitRegistration(
        organizationName: String,
        code: String
    ): Single<Response<RegSubmitResponse>> {
        return registrationService.submitRegistration(
            RegistrationSubmitBody(organizationName, code, asymmetricKey?.publicKey.orEmpty())
        )
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            .asyncToUiSingle()
    }

    fun submit(orgName: String, regCode: String): Single<Response<OnBoardingResponse>> {

        val orgField = Field.create(Field.ORGANIZATION_CODE, orgName).apply {
            editable = false
            visible = false
        }

        val regCodeField = Field.create(Field.REGISTRATION_CODE, regCode).apply {
            editable = false
        }

        val pk = asymmetricKey?.publicKey.orEmpty()
        val publicKey = Field.create(Field.FIELD_NAME_PUBLIC_KEY, pk)

        fieldsMap[orgField] = orgField.value
        fieldsMap[regCodeField] = regCodeField.value
        fieldsMap[publicKey] = publicKey.value

        return nclRegRepo.onBoarding(fieldsMap)
    }
}