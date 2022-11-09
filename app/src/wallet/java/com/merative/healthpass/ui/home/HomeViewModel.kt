package com.merative.healthpass.ui.home

import com.merative.healthpass.models.region.Env
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.flatMapIterable
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val packageDB: PackageDB,
    private val contactDB: ContactDB,
    private val issuerRepo: IssuerRepo,
    private val environmentHandler: EnvironmentHandler
) : BaseViewModel() {

    lateinit var issuerListJson: String

    fun loadData(): Single<Pair<List<Package>, List<ContactPackage>>> {
        return Single.zip(
            loadCredentials(),
            loadContacts(),
            { t1, t2 ->
                t1 to t2
            }
        )
    }

    private fun loadCredentials(): Single<List<Package>> {
        return packageDB.loadAll()
            .map { it.dataList }
            .toObservable()
            .flatMapIterable()
            .concatMap { setIssuerName(it) }
            .toList()
    }

    private fun setIssuerName(aPackage: Package): Observable<Package> {
        if (aPackage.verifiableObject.type == VCType.SHC) {
            return issuerRepo.getIssuerFromDb(aPackage.verifiableObject)
                .toObservable()
                .map { aPackage.issuerName = it.issuerName }
                .map { aPackage }
                .onErrorReturn { aPackage }
        }
        return Observable.just(aPackage)
    }

    fun getEnvironment(): Env {
        return environmentHandler.currentEnv
    }

    private fun loadContacts(): Single<ArrayList<ContactPackage>> {
        return contactDB.loadAll().map { it.dataList }
    }
}