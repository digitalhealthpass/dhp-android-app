package com.merative.healthpass.ui.contactDetails.associatedData

import com.merative.healthpass.models.api.registration.uploadCredential.AssociatedData
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.network.repository.UDCredentialsRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.asyncToUiSingle
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class AssociatedDataVM @Inject constructor(
    private val udCredentialsRepo: UDCredentialsRepo
) : BaseViewModel() {

    fun downloadAssociatedDataString(contactPackage: ContactPackage): Single<String> {
        return udCredentialsRepo.downloadAssociatedDataSting(contactPackage).asyncToUiSingle()
    }

    fun downloadAssociatedData(contactPackage: ContactPackage): Single<AssociatedData> {
        return udCredentialsRepo.downloadAssociatedData(contactPackage).asyncToUiSingle()
    }
}