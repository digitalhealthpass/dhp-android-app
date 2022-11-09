package com.merative.healthpass.ui.contactDetails.details

import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.download.DownloadResponse
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.network.repository.UDCredentialsRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class ContactDetailsVM @Inject constructor(
    private val nihRegRepo: NIHRegRepo,
    private val udCredentialsRepo: UDCredentialsRepo,
    private val contactDB: ContactDB,
    private val packageDB: PackageDB,
    private val environmentHandler: EnvironmentHandler,
) : BaseViewModel() {

    fun loadUploadedCredentials(contact: ContactPackage?): Single<ArrayList<Package>> {
        return packageDB.loadAll()
            .map { packageTable ->
                val credentialMap =
                    contact?.uploadedCredentialList.toHashMapWithValue { it.credentialId }

                val filteredTable = ArrayList<Package>()
                packageTable.dataList.forEach { pack ->
                    when {
                        //Verifying IDHP
                        pack.verifiableObject.credential?.id != null -> {
                            if (credentialMap.containsKey(pack.verifiableObject.credential?.id)) {
                                filteredTable.add(pack)
                            }
                        }
                        //Verifying DCC
                        pack.verifiableObject.cose?.cwt != null -> {
                            pack.verifiableObject.cose?.cwt?.certificate?.vaccinations?.forEach {
                                it.certificateIdentifier.let { id ->
                                    if (credentialMap.containsKey(id)) {
                                        filteredTable.add(pack)
                                    }
                                }
                            }
                        }
                        else -> {
                            //Verifying SHC
                            credentialMap.forEach {
                                if (it.key != null && it.key.isNumeric()) {
                                    it.key.toDouble().let { id ->
                                        if (id == pack.verifiableObject.jws?.payload?.nbf) {
                                            filteredTable.add(pack)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                filteredTable
            }
    }

    fun loadDownloadedCredentials(contact: ContactPackage?): Single<ArrayList<Package>> {
        return packageDB.loadAll()
            .map { table ->
                val map = contact?.downloadedCredentialList.toHashMapWithValue { it.id }

                table.dataList.filter {
                    map.containsKey(it.verifiableObject.credential?.id)
                }.toArrayList()
            }
    }

    fun downloadCredentials(contactPackage: ContactPackage): Single<Response<DownloadResponse>> {
        return udCredentialsRepo.downloadCredentials(contactPackage)
            .flatMap { response ->
                if (response.isSuccessfulAndHasBody()) {
                    updateContact(contactPackage, response)
                } else {
                    Single.just(response)
                }
            }
    }

    private fun updateContact(
        contactPackage: ContactPackage,
        response: Response<DownloadResponse>,
    ): Single<Response<DownloadResponse>> {
        val key = contactPackage.getSymmetricKey()

        val credentialDownloaded = response.body()!!
            .payload.attachments.mapNotNull { attachment ->
                attachment.toCredential(key)?.toCredentialDownloaded()
            }
            .toArrayList()

        contactPackage.downloadedCredentialList.addOrReplaceAll(credentialDownloaded)

        return contactDB.insert(contactPackage, true)
            .map {
                response
            }

    }

    fun deleteContact(contactPackage: ContactPackage): Maybe<Response<BaseResponse>> {
        return nihRegRepo.offBoarding(contactPackage, null)
            .flatMapMaybe { response ->
                if (response.isSuccessful) {
                    deleteContactDB(contactPackage).toMaybe()
                } else {
                    Maybe.just(response)
                }
            }
    }

    fun deleteContactDB(contactPackage: ContactPackage): Completable {
        return contactDB.delete(contactPackage)
    }

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()
}