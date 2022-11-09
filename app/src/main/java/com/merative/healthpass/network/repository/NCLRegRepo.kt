package com.merative.healthpass.network.repository

import com.merative.healthpass.models.Location
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import com.merative.healthpass.network.serviceinterface.NclRegistrationService
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.isLocation
import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class NCLRegRepo @Inject constructor(
    private val service: NclRegistrationService,
) {

    fun validateCode(orgName: String, code: String): Single<Response<BaseResponse>> {
        return service.validateCode(orgName, code)
            .asyncToUiSingle()
    }

    fun onBoarding(
        fieldsMap: HashMap<Field, Any?>
    ): Single<Response<OnBoardingResponse>> {
        return service.onBoarding(
            getBody(fieldsMap)
        ).asyncToUiSingle()
    }

    private fun getBody(fieldsMap: HashMap<Field, Any?>): RequestBody {
        val map = HashMap<String, Any?>()
        fieldsMap.forEach { (key, value) ->
            when {
                key.isLocation() -> {
                    map[key.path] = value?.let { (value as Location).code } ?: ""
                }
                //For NCL ID is the Public Key
                key.path == Field.ID -> {
                    //adjust the Key
                    map[Field.ID] = value.toString()
                }
                else -> {
                    map[key.path] = value
                }
            }
        }

        return JSONObject(map).toString().toRequestBody()
    }
}