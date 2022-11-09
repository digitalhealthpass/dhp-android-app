package com.merative.healthpass.ui.region

import com.merative.healthpass.models.region.Env
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.*
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class RegionSelectionVM @Inject constructor(
    private val environmentHandler: EnvironmentHandler,
    private val packageDB: PackageDB,
    private val issuerDB: IssuerDB,
    private val issuerMetadataDB: IssuerMetadataDB,
    private val schemaDB: SchemaDB,
) : BaseViewModel() {
    var currentEnv: Env? = null

    init {
        currentEnv = environmentHandler.currentEnv
    }

    fun getRegionList(): List<Env> = environmentHandler.envList

    fun saveRegion(): Completable {
        return if (currentEnv == null) {
            Completable.complete()
        } else {
            Completable.fromCallable { environmentHandler.putEnvironment(currentEnv!!) }
                .andThen(packageDB.deleteAll())
                .andThen(issuerDB.deleteAll())
                .andThen(issuerMetadataDB.deleteAll())
                .andThen(schemaDB.deleteAll())
        }
    }
}