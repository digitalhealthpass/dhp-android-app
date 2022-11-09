package com.merative.healthpass.ui.contactDetails.revoke

import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.uploadCredential.UploadDocResponse
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.ContactDB
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class ConsentRevokeVM @Inject constructor(
    private val repo: NIHRegRepo,
    private val contactsDB: ContactDB,
) : BaseViewModel() {
    fun requestConsentReceiptAndSubmit(
        contactPackage: ContactPackage,
        canRevoke: Boolean
    ): Single<Response<BaseResponse>> {

        return repo.requestConsentRevoke(contactPackage)
            .flatMap {
                if (it.isSuccessful) {
                    uploadDocument(contactPackage, canRevoke)
                } else {
                    Single.error(HttpException(it))
                }
            }
    }

    private fun uploadDocument(
        contactPackage: ContactPackage,
        canRevoke: Boolean,
    ): Single<Response<BaseResponse>> {
        return if (canRevoke) {
            repo.uploadDocument(contactPackage)
                .flatMap {
                    if (it.isSuccessful) {
                        offBoarding(contactPackage, it.body())
                    } else {
                        Single.error(HttpException(it))
                    }
                }
        } else {
            offBoarding(contactPackage, null)
        }
    }

    private fun offBoarding(
        contactPackage: ContactPackage,
        response: UploadDocResponse?
    ): Single<Response<BaseResponse>> {
        return repo.offBoarding(contactPackage, response)
            .flatMap { submitResponse ->
                if (submitResponse.isSuccessful) {
                    deleteContactFromDB(contactPackage)
                        .andThen(Single.just(submitResponse))
                } else {
                    Single.just(submitResponse)
                }
            }
    }

    fun deleteContactFromDB(contactPackage: ContactPackage): Completable {
        return contactsDB.delete(contactPackage)
    }
}