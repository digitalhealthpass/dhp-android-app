package com.merative.healthpass.ui.mainActivity

import com.merative.healthpass.common.VerifierConstants
import com.merative.healthpass.models.OrganizationException
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.ConfigRepo
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.network.repository.LoginRepo
import com.merative.healthpass.network.repository.MetricRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.MetricDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.credential.getConfigId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getConfigVersion
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

class FlavorVM @Inject constructor(
    private val packageDB: PackageDB,
    private val loginRepo: LoginRepo,
    private val metricDB: MetricDB,
    private val metricRepo: MetricRepo,
    private val configRepo: ConfigRepo,
    private val issuerRepo: IssuerRepo,
) : BaseViewModel() {
    var selectedCredential: Package? = null
    var isAnotherCredentialAvailable = false

    fun loadCurrentCredential(): Single<Optional<Package>> {
        return if (selectedCredential != null) {
            Single.just(Optional.ofNullable(selectedCredential))
        } else {
            packageDB.loadAll()
                .map { table ->
                    selectedCredential = table.dataList.firstOrNull { it.isSelected }

                    if (selectedCredential == null || selectedCredential?.verifiableObject?.credential?.isExpired() == true) {
                        val foundCredNonExpired =
                            table.dataList.firstOrNull { !it.verifiableObject.credential!!.isExpired() }
                        if (foundCredNonExpired != null) {
                            isAnotherCredentialAvailable = true
                        }
                    }

                    Optional.ofNullable(selectedCredential)
                }.flatMap {
                    if (it.isPresent) {
                        loginRepo.loginWithCredential(it.get()).flatMap { updateConfigCache() }
                            .flatMap { updateSignatureKeysIfNeed(it) }
                            .flatMap { submitMetric() }
                            .map { Optional.ofNullable(selectedCredential) }
                    } else {
                        return@flatMap Single.just(Optional.ofNullable(selectedCredential))
                    }
                }
        }
    }

    private fun updateConfigCache(): Single<Boolean> {
        val id = selectedCredential?.verifiableObject?.credential?.getConfigId()
            ?: throw OrganizationException("cannot find config ID")
        val version = selectedCredential?.verifiableObject?.credential?.getConfigVersion()
            ?: throw OrganizationException("cannot find config version")
        return configRepo.updateConfigCache(id, version)
    }

    private fun updateSignatureKeysIfNeed(needUpdate: Boolean) =
        if (needUpdate) {
            issuerRepo.loadAndSaveSignatureKeys()
        } else {
            Single.just(needUpdate)
        }

    fun credentialsDeleted() {
        selectedCredential = null
    }

    fun credentialDeleted(aPackage: Package) {
        if (aPackage == selectedCredential)
            selectedCredential = null
    }

    private fun submitMetric(): Single<Unit> {
        return metricDB.loadAll()
            .flatMap { metricTable ->
                if (metricTable.dataList.isEmpty()) {
                    //if list is empty, then avoid sending request
                    Single.just(false to 0)
                } else {
                    metricRepo.submitMetrics(metricTable.dataList)
                        .map { Pair(it.isSuccessful, metricTable.dataList.size) }
                }
            }.flatMapCompletable {
                if (it.first ||
                    (!it.first && it.second >= VerifierConstants.MetricsUploadDeleteCount)
                ) {
                    //Check if Metrics goes over 1000 count in case of failure
                    metricDB.deleteAll()
                } else {
                    Completable.complete()
                }
            }.andThen(Single.just(Unit))
    }
}