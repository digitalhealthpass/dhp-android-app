package com.merative.healthpass.network.repository

import com.google.gson.JsonObject
import com.merative.healthpass.extensions.encrypt
import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.models.Location
import com.merative.healthpass.models.api.ApiDataException
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import com.merative.healthpass.models.api.registration.uploadCredential.ConsentReceiptResponse
import com.merative.healthpass.models.api.registration.uploadCredential.UploadDocRequestBody
import com.merative.healthpass.models.api.registration.uploadCredential.UploadDocResponse
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.network.serviceinterface.NihRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.isLocation
import com.merative.watson.healthpass.verifiablecredential.models.credential.getLinkId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getPassCode
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class NIHRegRepo @Inject constructor(
    private val service: NihRegistrationService,
) {
    private var consentReceiptJson: JsonObject? = null

    private val utcMilliSeconds: Long
        get() {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000
        }

    fun displaySchema(orgName: String): Single<Response<DisplaySchemaResponse>> {
        return service.displaySchema(orgName)
            .asyncToUiSingle()
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
    }

    fun validateCode(orgName: String, code: String): Single<Response<BaseResponse>> {
        return service.validateCode(orgName, code)
            .asyncToUiSingle()
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
    }

    //region on boarding
    fun onBoarding(
        orgName: String,
        fieldsMap: HashMap<Field, Any?>
    ): Single<Response<OnBoardingResponse>> {
        return service.onBoarding(
            orgName,
            getBody(fieldsMap)
        ).asyncToUiSingle()
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
    }

    private fun getBody(fieldsMap: HashMap<Field, Any?>): RequestBody {
        val map = HashMap<String, Any?>()
        fieldsMap.forEach { (key, value) ->
            when {
                key.isLocation() -> {
                    map[key.path] = (value as? Location)?.code
                }
                key.path == Field.KEY -> {
                    //adjust the Key
                    map[Field.FIELD_NAME_PUBLIC_KEY] = value?.toString()
                }
                else -> {
                    map[key.path] = value
                }
            }
        }

        return JSONObject(map).toString().toRequestBody()
    }
    //endregion

    fun requestConsentRevoke(contactPackage: ContactPackage): Single<Response<ConsentReceiptResponse>> {
        if (contactPackage.asymmetricKey == null)
            return Single.error(ApiDataException("AsymmetricKey is null"))

//        val publicKeyEncoded = URLEncoder.encode(contactPackage.asymmetricKey.publicKey, "utf-8")
        return service.getConsentRevoke(
            contactPackage.getOrgId(),
            contactPackage.asymmetricKey.publicKey
        ).doOnSuccess {
            if (it.isSuccessfulAndHasBody()) {
                consentReceiptJson = it.body()?.payload
            }
        }.asyncToUiSingle()
    }

    //region upload document
    fun uploadDocument(
        contactPackage: ContactPackage,
    ): Single<Response<UploadDocResponse>> {
        return getUploadDocBody(contactPackage)
            .flatMap {
                service.uploadDocument(contactPackage.getDocumentURL(), it)
                    .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            }
            .asyncToUiSingle()
    }

    private fun getUploadDocBody(
        contactPackage: ContactPackage,
    ): Single<UploadDocRequestBody> {
        return Single.fromCallable {
            if (consentReceiptJson == null) throw ApiDataException("consentReceiptResponse is null")

            val jsonObject = UDCredentialsRepo.constructConsentReceipt(
                contactPackage,
                consentReceiptJson
            )

            val firstService = consentReceiptJson
                ?.getAsJsonArray("services")?.get(0)?.toString()

            val symmetricKey = contactPackage.getSymmetricKey()

            when {
                firstService == null -> {
                    throw ApiDataException("firstService is null")
                }

                symmetricKey == null -> {
                    throw ApiDataException("symmetricKey is null")
                }
                else -> {

                    //the name is custom but time need to be utc milliseconds to align with ios and backend
                    val name = "$firstService $utcMilliSeconds"

                    val algorithm = symmetricKey.algorithm
                    val iv = symmetricKey.iv
                    val value = symmetricKey.keyValue

                    UploadDocRequestBody(
                        link = contactPackage.profilePackage?.verifiableObject?.credential?.getLinkId().orEmpty(),
                        password = contactPackage.profilePackage?.verifiableObject?.credential?.getPassCode()
                            .orEmpty(),
                        name = name,
                        content = jsonObject.toString().encrypt(algorithm, iv, value)
                    )
                }
            }
        }
    }
    //endregion

    //region off boarding
    fun offBoarding(
        contactPackage: ContactPackage,
        uploadDocResponse: UploadDocResponse?
    ): Single<Response<BaseResponse>> {
        if (uploadDocResponse != null) {
            val singedConsentReceipt =
                UDCredentialsRepo.constructConsentReceipt(contactPackage, consentReceiptJson)

            return service.offBoardingAndRevoke(
                publicKey = contactPackage.getIdCredSubjectKey(),
                publicKeyType = UDCredentialsRepo.PUBLIC_KEY_TYPE,
                documentId = uploadDocResponse.payload?.id,
                linkId = uploadDocResponse.payload?.link.orEmpty(),
                org = contactPackage.getOrgId(),
                body = singedConsentReceipt.toString()
                    .toRequestBody("application/json".toMediaType())
            ).asyncToUiSingle()
        } else {
            return service.offBoarding(
                contactPackage.getIdCredSubjectKey(),
                contactPackage.getOrgId(),
            ).asyncToUiSingle()
        }
    }
    //endregion
}