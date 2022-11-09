package com.merative.healthpass.network.repository

import android.util.Base64
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.merative.healthpass.extensions.encrypt
import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.sign
import com.merative.healthpass.models.api.ApiDataException
import com.merative.healthpass.models.api.registration.download.DownloadResponse
import com.merative.healthpass.models.api.registration.uploadCredential.*
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.serviceinterface.NihRegistrationService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.watson.healthpass.verifiablecredential.extensions.format
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.credential.Proof
import com.merative.watson.healthpass.verifiablecredential.models.credential.getLinkId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getPassCode
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class UDCredentialsRepo @Inject constructor(
    private val service: NihRegistrationService,
) {
    private val utcMilliSeconds: Long
        get() {
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000
        }

    private var consentReceiptJson: JsonObject? = null

    fun getConsentReceipt(
        contactPackage: ContactPackage
    ): Single<Response<ConsentReceiptResponse>> {
        val orgId = contactPackage.getOrgId()

        val key = contactPackage.getIdCredSubjectKey().orEmpty()

        return service.getConsentReceipt(orgId, key)
            .asyncToUiSingle()
            .doOnSuccess {
                if (it.isSuccessfulAndHasBody()) {
                    consentReceiptJson = it.body()?.payload
                }
            }
    }

    //region upload document
    fun uploadDocument(
        contactPackage: ContactPackage,
        selectedPackages: List<Package>
    ): Single<Response<UploadDocResponse>> {
        return getUploadDocBody(contactPackage, selectedPackages)
            .flatMap {
                service.uploadDocument(contactPackage.getDocumentURL(), it)
                    .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            }
            .asyncToUiSingle()

    }

    private fun getUploadDocBody(
        contactPackage: ContactPackage,
        selectedPackages: List<Package>,
    ): Single<UploadDocRequestBody> {
        return Single.fromCallable {
            if (consentReceiptJson == null) throw ApiDataException("consentReceiptResponse is null")

            val jsonArray = JsonArray()

            //order is important for this JSON Array
            jsonArray.add(constructContactId(contactPackage))

            jsonArray.add(constructConsentReceipt(contactPackage, consentReceiptJson))

            selectedPackages.forEach {
                if (it.verifiableObject.type == VCType.IDHP ||
                    it.verifiableObject.type == VCType.GHP ||
                    it.verifiableObject.type == VCType.VC
                ) {
                    jsonArray.add(it.verifiableObject.credential?.toJsonElement())
                } else if (it.verifiableObject.type == VCType.SHC) {
                    jsonArray.add(it.verifiableObject.jws?.jws)
                } else if (it.verifiableObject.type == VCType.DCC) {
                    jsonArray.add(it.verifiableObject.rawString)
                }
            }

            val firstService = consentReceiptJson
                ?.getAsJsonArray("services")?.get(0)?.asJsonObject

            val symmetricKey = contactPackage.getSymmetricKey()

            when {
                firstService == null -> {
                    throw ApiDataException("firstService is null")
                }

                symmetricKey == null -> {
                    throw ApiDataException("symmetricKey is null")
                }
                else -> {

                    val linkId =
                        contactPackage.profilePackage?.verifiableObject?.credential?.getLinkId()
                    val passCode =
                        contactPackage.profilePackage?.verifiableObject?.credential?.getPassCode()

                    //the name is custom but time need to be utc milliseconds to align with ios and backend
                    val name = firstService.asJsonObject?.getStringOrNull("service") +
                            " $utcMilliSeconds"

                    val algorithm = symmetricKey.algorithm
                    val iv = symmetricKey.iv
                    val value = symmetricKey.keyValue

                    UploadDocRequestBody(
                        link = linkId.orEmpty(),
                        password = passCode.orEmpty(),
                        name = name,
                        content = jsonArray.toString().encrypt(algorithm, iv, value)
                    )
                }
            }
        }
    }

    private fun constructContactId(contactPackage: ContactPackage): JsonObject {
        return contactPackage.idPackage?.verifiableObject?.credential?.toJsonElement()?.asJsonObject
            ?: throw ApiDataException("constructContactId is null")
    }

    //endregion

    //region submit data
    fun submitData(
        contactPackage: ContactPackage,
        response: UploadDocResponse?
    ): Single<Response<SubmitDataResponse>> {
        return getSubmitBody(contactPackage, response)
            .flatMap {
                service.submitData(it)
                    .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
            }
            .asyncToUiSingle()
    }

    private fun getSubmitBody(
        contactPackage: ContactPackage,
        response: UploadDocResponse?
    ): Single<SubmitDataRequestBody> {
        return Single.fromCallable {
            val orgName = contactPackage.getOrgId()

            if (response?.payload == null)
                throw ApiDataException("upload document response is null")

            val publicKey = contactPackage.getIdCredSubjectKey().orEmpty()
            val documentId = response.payload.id

            SubmitDataRequestBody(
                orgName,
                publicKey,
                documentId,
                PUBLIC_KEY_TYPE,
                response.payload.link,
            )
        }
    }
    //endregion

    //region download credentials
    fun downloadCredentials(contactPackage: ContactPackage): Single<Response<DownloadResponse>> {
        val downloadJsonObject: JsonObject? =
            contactPackage.profilePackage?.verifiableObject?.credential?.credentialSubject
                ?.getAsJsonObject("technical")
                ?.asJsonObject?.getAsJsonObject("download")

        val url =
            downloadJsonObject?.getStringOrNull("url") ?: return apiError("linkId is null")

        val passCode =
            downloadJsonObject.getStringOrNull("passcode") ?: return apiError("passcode is null")

        return service.downloadCredentials(url, passCode)
            .asyncToUiSingle()
    }
    //endregion

    //region associated data
    fun downloadAssociatedData(contactPackage: ContactPackage): Single<AssociatedData> {
        if (contactPackage.canDownload()) {
            return loadAssociatedDataAttachment(contactPackage)
                .map {
                    if (it.isSuccessfulAndHasBody()) {
                        AssociatedData(postBoxPayload = it.body()?.payload)
                    } else {
                        throw HttpException(it)
                    }
                }
        } else {
            return Single.zip(
                loadAssociatedData(contactPackage),
                loadAssociatedDataAttachment(contactPackage),
                { dataResponse, attachmentResponse ->
                    val data = dataResponse.body()
                    val attachment = attachmentResponse.body()

                    if (dataResponse.isSuccessfulAndHasBody() || attachmentResponse.isSuccessfulAndHasBody()) {
                        AssociatedData(data?.payload, attachment?.payload)
                    } else {
                        if (data == null)
                            throw HttpException(dataResponse)
                        else
                            throw HttpException(attachmentResponse)
                    }
                }
            )
        }
    }

    fun downloadAssociatedDataSting(contactPackage: ContactPackage): Single<String> {
        return downloadAssociatedData(contactPackage)
            .map(this::getAssociatedDataString)
    }

    private fun loadAssociatedDataAttachment(contactPackage: ContactPackage): Single<Response<AssociatedPostBoxDataResponse>> {
        return service.downloadAssociatedDataAttachment(
            contactPackage.profilePackage?.verifiableObject?.credential?.getPassCode(),
            contactPackage.profilePackage?.verifiableObject?.credential?.getLinkId(),
        )
    }

    private fun loadAssociatedData(contactPackage: ContactPackage): Single<Response<AssociatedCosDataResponse>> {
        return Single.fromCallable {
            val json = JsonObject()

            val proof = Proof(
                creator = contactPackage.asymmetricKey?.publicKey,
                created = null,
                nonce = null,
                signatureValue = null,
                type = null,
                jws = null,
                verificationMethod = null,
                proofPurpose = null,
                domain = null,
                challenge = null,
                issuerData = null,
                attributes = null,
                signature = null,
                signatureCorrectnessProof = null,
            )
            json.add("proof", proof.toJsonElement())
            json.sign(contactPackage.asymmetricKey)
        }.flatMap {
            service.downloadAssociatedData(
                contactPackage.getOrgId().toLowerCase(),
                contactPackage.asymmetricKey?.publicKey,
                PUBLIC_KEY_TYPE,
                it
            )
        }
            .onErrorResumeNext { t -> RxHelper.handleErrorSingle(t) }
    }

    private fun getAssociatedDataString(associatedDataResponse: AssociatedData?): String {
        val list = mutableListOf<JsonElement?>()
        associatedDataResponse?.cosPayload?.let(list::add)
        associatedDataResponse?.postBoxPayload?.let(list::add)

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(list.toJsonElement())
    }
    //endregion

    private fun <T> apiError(message: String) = Single.error<T>(ApiDataException(message))

    companion object {
        private const val CONSENT_PROOF_TYPE = "CKM_SHA256_RSA_PKCS_PSS"

        private const val PROOF_CREATED_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

        const val PUBLIC_KEY_TYPE = "spki"

        fun constructConsentReceipt(
            contactPackage: ContactPackage,
            consentReceiptJson: JsonObject?
        ): JsonObject {
            val asymmetricKey = contactPackage.asymmetricKey

            val consentReceiptJSON =
                consentReceiptJson ?: throw ApiDataException("Consent Receipt Not found")

            val creator = Base64.encodeToString(
                asymmetricKey?.publicKey?.toByteArray(),
                Base64.DEFAULT or Base64.NO_WRAP
            ).trim()

            val proof = Proof(
                type = CONSENT_PROOF_TYPE,
                created = Date().format(PROOF_CREATED_FORMAT),
                creator = creator,
                signatureValue = null,
                nonce = null,
                jws = null,
                verificationMethod = null,
                proofPurpose = null,
                domain = null,
                challenge = null,
                issuerData = null,
                attributes = null,
                signature = null,
                signatureCorrectnessProof = null,
            )

            //remove the existing empty proof object from response
            consentReceiptJSON.remove("proof")
            //add the generated proof object
            consentReceiptJSON.add("proof", proof.toJsonElement())

            val signatureValue = consentReceiptJSON.sign(asymmetricKey)

            consentReceiptJSON.getAsJsonObject("proof")
                .addProperty("signatureValue", signatureValue)

            return consentReceiptJSON
        }
    }
}