package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.download.DownloadResponse
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import com.merative.healthpass.models.api.registration.uploadCredential.*
import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

@JvmSuppressWildcards
interface NihRegistrationService {

    @GET("datasubmission/organization/{orgName}/displayschemaid")
    fun displaySchema(
        @Path("orgName") orgName: String
    ): Single<Response<DisplaySchemaResponse>>

    @GET("datasubmission/onboarding/{orgName}/validatecode/{code}")
    fun validateCode(
        @Path("orgName") orgName: String,
        @Path("code") code: String,
    ): Single<Response<BaseResponse>>

    @POST("datasubmission/onboarding/{orgName}")
    fun onBoarding(
        @Path("orgName") orgName: String,
        @Body body: RequestBody
    ): Single<Response<OnBoardingResponse>>

    @DELETE("datasubmission/onboarding/{org}")
    fun offBoarding(
        @Header("x-hpass-datasubmission-key") key: String?,
        @Path("org") orgPath: String,
    ): Single<Response<BaseResponse>>

    @HTTP(method = "DELETE", path = "datasubmission/onboarding/{org}", hasBody = true)
    fun offBoardingAndRevoke(
        @Header("x-hpass-datasubmission-key") publicKey: String?,
        @Header("x-hpass-key-type") publicKeyType: String?,
        @Header("x-hpass-document-id") documentId: String?,
        @Header("x-hpass-link-id") linkId: String,
        @Path("org") org: String,
        @Body body: RequestBody,
    ): Single<Response<BaseResponse>>

    @GET("datasubmission/organization/{DATASUBMISSION_ORG}/consentRevoke/{encodedPublicKey}")
    fun getConsentRevoke(
        @Path("DATASUBMISSION_ORG") orgName: String,
        @Path("encodedPublicKey", encoded = false) encodedPublicKey: String,
    ): Single<Response<ConsentReceiptResponse>>

    //region upload download
    @GET("datasubmission/organization/{orgName}/consentReceipt/{code}")
    fun getConsentReceipt(
        @Path("orgName") orgName: String,
        @Path("code") code: String
    ): Single<Response<ConsentReceiptResponse>>

    //https://dev2.wh-hpass.dev.acme.com/api/v1/datasubmission/data/upload
    /**
     * this is mostly used for uploading a consent receipt then should be followed by another API
     */
    @POST
    fun uploadDocument(
        @Url url: String,
        @Body body: UploadDocRequestBody,
    ): Single<Response<UploadDocResponse>>

    @POST("datasubmission/data/submit")
    fun submitData(
        @Body body: SubmitDataRequestBody,
    ): Single<Response<SubmitDataResponse>>

    //https://dev2.wh-hpass.dev.acme.com/api/v1/postbox/ap1/v1/links/{linkId}
    @GET
    fun downloadCredentials(
        @Url url: String,
        @Header("x-postbox-access-token") passCode: String
    ): Single<Response<DownloadResponse>>
    //endregion

    @GET("datasubmission/cos/{org}/owner/{holderID}")
    fun downloadAssociatedData(
        @Path("org") org: String,
        @Path("holderID") holderID: String?,
        @Query("publicKeyType") publicKeyType: String,
        @Query("signatureValue") signatureValue: String?,
        @Query("format") format: String = "json"
    ): Single<Response<AssociatedCosDataResponse>>

    @GET("postbox/api/v1/links/{linkId}/attachments")
    fun downloadAssociatedDataAttachment(
        @Header("x-postbox-access-token") passCode: String?,
        @Path("linkId") linkId: String?,
        @Query("format") format: String = "json"
    ): Single<Response<AssociatedPostBoxDataResponse>>
}