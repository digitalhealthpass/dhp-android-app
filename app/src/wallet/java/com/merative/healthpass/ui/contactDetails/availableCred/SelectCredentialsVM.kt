package com.merative.healthpass.ui.contactDetails.availableCred

import com.merative.healthpass.extensions.isNumeric
import com.merative.healthpass.extensions.toHashMapWithValue
import com.merative.healthpass.models.credential.SelectCredentials
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.flatMapIterable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class SelectCredentialsVM @Inject constructor(
    private val packageDB: PackageDB,
    private val issuerRepo: IssuerRepo
) : BaseViewModel() {
    /**use function [addOrRemoveCredentials] and avoid doing changes to the list itself*/
    val selectedCredentials = ArrayList<Package>()
    val isListEmpty: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(selectedCredentials.isEmpty())

    val containsUploadedCards: Boolean
        get() = selectedCredentials.map { unavailable.contains(it) }.contains(true)
    private val unavailable = ArrayList<Package>()

    /**
     * @return [Pair] of 2 list, first is available credentials, second is the unavailable ones
     */
    fun loadAllCredentials(contact: ContactPackage?): Single<SelectCredentials> {
        return packageDB.loadAll()
            .map { it.dataList }
            .toObservable()
            .flatMapIterable()
            .concatMap { setIssuerName(it) }
            .toList()
            .map { dataList ->
                val available = ArrayList<Package>()
                val unavailable = ArrayList<Package>()
                val unsupported = ArrayList<Package>()
                val map = contact?.uploadedCredentialList.toHashMapWithValue { it.credentialId }

                dataList.forEach { pack ->
                    pack.verifiableObject.credential?.id?.let { idhpId ->
                        if (map.containsKey(idhpId)) {
                            unavailable.add(pack)
                        } else {
                            available.add(pack)
                        }
                    }

                    pack.verifiableObject.jws?.payload?.nbf?.let { shcId ->
                        val keys = map.keys
                            .mapNotNull { it?.takeIf(String::isNumeric) }
                            .map { it.toDouble() }

                        if (keys.contains(shcId)) {
                            keys.forEach { id ->
                                if (id == shcId) {
                                    unavailable.add(pack)
                                } else {
                                    available.add(pack)
                                }
                            }
                        } else {
                            available.add(pack)
                        }
                    }

                    pack.verifiableObject.cose?.cwt?.certificate?.vaccinations?.forEach { it ->
                        it.certificateIdentifier.takeIf { it.isNotEmpty() }?.let { id ->
                            if (map.containsKey(id)) {
                                unavailable.add(pack)
                            } else {
                                available.add(pack)
                            }
                        }
                    }
                }
                this.unavailable.clear()
                this.unavailable.addAll(unavailable)

                SelectCredentials(available, unavailable, unsupported)
            }
    }

    private fun setIssuerName(aPackage: Package): Observable<Package> {
        if (aPackage.verifiableObject.type == VCType.SHC) {
            return issuerRepo.getIssuerFromDb(aPackage.verifiableObject)
                .toObservable()
                .map { aPackage.issuerName = it.issuerName }
                .map { aPackage }
        }
        return Observable.just(aPackage)
    }

    fun addOrRemoveCredentials(aPackage: Package) {
        if (selectedCredentials.contains(aPackage)) {
            selectedCredentials.remove(aPackage)
        } else {
            selectedCredentials.add(aPackage)
        }
        isListEmpty.onNext(selectedCredentials.isEmpty())
    }
}