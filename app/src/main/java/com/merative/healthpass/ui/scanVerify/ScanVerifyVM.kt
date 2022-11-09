package com.merative.healthpass.ui.scanVerify

import androidx.lifecycle.ViewModel
import com.merative.healthpass.extensions.replaceIndex
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.metadata.MetaResponse
import com.merative.healthpass.models.api.schema.SchemaResponse
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.network.repository.SchemaRepo
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.credential.isId
import com.merative.watson.healthpass.verifiablecredential.models.credential.isProfile
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.isIDHPorGHPorVC
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class ScanVerifyVM @Inject constructor(
    private val schemaRepo: SchemaRepo,
    private val nihRegRepo: NIHRegRepo,
    private val contactDB: ContactDB,
    private val packageDB: PackageDB,
    private val issuerRepo: IssuerRepo
) : ViewModel() {
    var packageList = ArrayList<Package>()
    var isContact = false
    var credentialsAvailable = false
    var organizationName = ""
    var asymmetricKey: AsymmetricKey? = null
    var contactPackage: ContactPackage? = null

    fun requestSchemaAndCheckCredentials(): Observable<Triple<Package?, Response<SchemaResponse>?, Response<MetaResponse>?>> =
        checkExistingCredentials().flatMapObservable { requestSchemaAndMetaData() }

    private fun checkExistingCredentials() =
        packageDB.loadAll().map { it.dataList.size >= 1 }.doOnSuccess { credentialsAvailable = it }

    private fun requestSchemaAndMetaData(): Observable<Triple<Package?, Response<SchemaResponse>?, Response<MetaResponse>?>> {
        return Observable.fromIterable(packageList)
            .flatMap { aPackage ->
                if (isContact) {
                    aPackage.verifiableObject.type = VCType.CONTACT
                }
                when {
                    aPackage.verifiableObject.isIDHPorGHPorVC || aPackage.verifiableObject.type == VCType.CONTACT -> {
                        schemaRepo.getSchemaAndMetaData(aPackage)
                            .toObservable()
                            .map {
                                if (!it.first.isSuccessful) {
                                    throw HttpException(it.first)
                                }

                                Triple(aPackage, it.first, it.second)
                            }
                    }
                    aPackage.verifiableObject.type == VCType.SHC -> {
                        issuerRepo.getIssuerFromDb(aPackage.verifiableObject)
                            .toObservable()
                            .map { aPackage.issuerName = it.issuerName }
                            .map { Triple(aPackage, null, null) }
                            .onErrorReturn { Triple(aPackage, null, null) }
                    }
                    else -> {
                        Observable.just(Triple(aPackage, null, null))
                    }
                }
            }
            .doOnNext { triple ->
                val index =
                    packageList.indexOf(triple.first)

                val aPackage = triple.first.copy(
                    schema = triple.second?.body()?.payload,
                    issuerMetaData = triple.third?.body()?.payload,
                )
                aPackage.issuerName = triple.first.issuerName

                if (index == -1)
                    throw NullPointerException("Couldn't find credential in list")

                packageList.replaceIndex(aPackage, index)

            }
    }

    fun savePackage(forceSave: Boolean): Single<Boolean> {
        return if (isContact) {
            contactPackage = createContact()
            contactDB.insert(contactPackage!!, forceSave)
        } else {
            packageDB.insertAll(packageList, forceSave)
        }
    }

    private fun createContact(): ContactPackage {
        //create the contact to be added
        var profileIndex = -1
        var idIndex = -1
        //look for the profile and id credentials
        packageList.forEachIndexed { index, aPackage ->
            if (aPackage.verifiableObject.credential!!.isProfile()) {
                profileIndex = index
            } else if (aPackage.verifiableObject.credential!!.isId()) {
                idIndex = index
            }
        }

        return ContactPackage(
            packageList.getOrNull(profileIndex),
            packageList.getOrNull(idIndex),
            asymmetricKey,
            organizationName
        )
    }

    fun offBoarding(): Single<Response<BaseResponse>> {
        return nihRegRepo.offBoarding(createContact(), null)
    }
}