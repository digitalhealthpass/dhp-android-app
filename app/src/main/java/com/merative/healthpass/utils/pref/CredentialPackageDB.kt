package com.merative.healthpass.utils.pref

import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.models.sharedPref.Package
import com.merative.watson.healthpass.verifiablecredential.extensions.getListOrNull
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers

class PackageDB(
    sharedPrefUtils: SharedPrefUtils
) : BaseDB<Package>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    fun migrateOldData() {
        Maybe.create<List<Package>> {
            val jsonString = sharedPrefUtils.getString("credentials_schemas")
            if (jsonString.isNotNullOrEmpty()) {
                it.onSuccess(
                    jsonString?.toJsonElement()?.asJsonObject
                        ?.getListOrNull<Package>("credentialAndSchemaList")
                        .orEmpty()
                )
            } else {
                it.onComplete()
            }
        }.subscribeOn(Schedulers.io())
            .flatMapSingle {
                insertAll(it, false)
            }
            .flatMapCompletable {
                Completable.fromCallable { sharedPrefUtils.clearPrefs("credentials_schemas") }
            }
            .subscribe({}, rxError("failed to migrate old db"))
    }

    companion object {
        const val VERSION = 1
        const val DB_NAME = "credential_schema"
    }
    //version 0, Package had Credential object
    //version 1. Credential Package replaced Credential object to Verifiable Object
}
