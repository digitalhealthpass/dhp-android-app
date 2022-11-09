package com.merative.healthpass.ui.settings

import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    sharedPrefUtils: SharedPrefUtils,
    packageDB: PackageDB,
    contactDB: ContactDB,
    environmentHandler: EnvironmentHandler,
    private val schemaDB: SchemaDB,
    private val issuerMetadataDB: IssuerMetadataDB,
    private val issuerDB: IssuerDB,
) : BaseSettingsViewModel(
    sharedPrefUtils, packageDB, contactDB, environmentHandler
) {
    fun resetCache(): Completable {
        return packageDB.loadAll()
            .map { table -> table.dataList.filter { it.isSelected } }
            .map { markPackagesUnselected(it) }
            .flatMap { packageDB.insertAll(it, true) }
            .flatMap { Single.just(issuerDB.deleteAll()) }
            .flatMap { Single.just(issuerMetadataDB.deleteAll()) }
            .flatMapCompletable { schemaDB.deleteAll() }
    }

    private fun markPackagesUnselected(list: List<Package>): List<Package> {
        list.map { it.isSelected = false }
        return list
    }
}