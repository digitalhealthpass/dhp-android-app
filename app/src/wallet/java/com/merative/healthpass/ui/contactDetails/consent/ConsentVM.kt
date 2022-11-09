package com.merative.healthpass.ui.contactDetails.consent

import com.merative.healthpass.extensions.addOrReplaceAll
import com.merative.healthpass.extensions.toArrayList
import com.merative.healthpass.models.api.registration.uploadCredential.ConsentReceiptResponse
import com.merative.healthpass.models.api.registration.uploadCredential.SubmitDataResponse
import com.merative.healthpass.models.api.registration.uploadCredential.UploadDocResponse
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.UDCredentialsRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.ContactDB
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class ConsentVM @Inject constructor(
    private val repo: UDCredentialsRepo,
    private val contactsDB: ContactDB,
) : BaseViewModel() {

    fun requestConsentReceipt(contactPackage: ContactPackage): Single<Response<ConsentReceiptResponse>> {
        return repo.getConsentReceipt(contactPackage)
    }

    fun uploadDocument(
        contactPackage: ContactPackage,
        selectedPackages: List<Package>
    ): Single<Response<SubmitDataResponse>> {
        return repo.uploadDocument(contactPackage, selectedPackages)
            .flatMap {
                if (it.isSuccessful) {
                    submitData(contactPackage, it.body())
                } else {
                    Single.error(HttpException(it))
                }
            }
    }

    private fun submitData(
        contactPackage: ContactPackage,
        response: UploadDocResponse?
    ): Single<Response<SubmitDataResponse>> {
        return repo.submitData(contactPackage, response)
            .flatMap { submitResponse ->
                if (submitResponse.isSuccessful) {
                    //if Successful then save in DB the credentials, then return the response
                    contactPackage.uploadedCredentialList
                        .addOrReplaceAll(
                            submitResponse.body()?.payload?.credentialsProcessed.toArrayList()
                        )
                    contactsDB.insert(contactPackage, true)
                        .map {
                            submitResponse
                        }
                } else {
                    Single.just(submitResponse)
                }
            }
    }
}