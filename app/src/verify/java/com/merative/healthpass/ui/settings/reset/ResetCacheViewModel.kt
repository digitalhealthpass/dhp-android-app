package com.merative.healthpass.ui.settings.reset

import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.asyncToUiCompletable
import com.merative.healthpass.utils.pref.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ResetCacheViewModel @Inject constructor(
    private val schemaDB: SchemaDB,
    private val packageDB: PackageDB,
    private val issuerMetadataDB: IssuerMetadataDB,
    private val issuerDB: IssuerDB,
    private val shcIssuerDB: ShcIssuerDB,
    private val dccIssuerDB: DccIssuerDB
) : BaseViewModel() {

    fun getSize(): Single<Long> {
        return Single.fromCallable {
            return@fromCallable schemaDB.getSize() + issuerDB.getSize() + shcIssuerDB.getSize() + dccIssuerDB.getSize() + issuerMetadataDB.getSize()
        }
    }

    fun resetCache(): Completable {
        return packageDB.loadAll()
            .map { table -> table.dataList.filter { it.isSelected } }
            .map { markPackagesUnselected(it) }
            .flatMap { packageDB.insertAll(it, true) }
            .flatMapCompletable { clearCacheComponents() }
            .asyncToUiCompletable()
    }

    private fun markPackagesUnselected(list: List<Package>): List<Package> {
        list.map { it.isSelected = false }
        return list
    }

    private fun clearCacheComponents() =
        Completable.concat(
            listOf(
                issuerDB.deleteAll(),
                shcIssuerDB.deleteAll(),
                dccIssuerDB.deleteAll(),
                issuerMetadataDB.deleteAll(),
                schemaDB.deleteAll()
            )
        )
}