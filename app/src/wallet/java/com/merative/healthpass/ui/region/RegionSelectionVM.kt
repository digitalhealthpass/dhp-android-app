package com.merative.healthpass.ui.region

import com.merative.healthpass.models.region.Env
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.IssuerMetadataDB
import com.merative.healthpass.utils.pref.SchemaDB
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class RegionSelectionVM @Inject constructor(
    private val environmentHandler: EnvironmentHandler,
    private val contactDB: ContactDB,
    private val packageDB: PackageDB,
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
                .andThen(contactDB.deleteAll())
                .andThen(packageDB.deleteAll())
                .andThen(issuerMetadataDB.deleteAll())
                .andThen(schemaDB.deleteAll())
        }
    }
}