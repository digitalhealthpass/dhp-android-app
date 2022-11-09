package com.merative.healthpass.ui.settings

import android.content.Context
import com.google.gson.JsonObject
import com.merative.healthpass.models.region.Env
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

open class BaseSettingsViewModel @Inject constructor(
    protected val sharedPrefUtils: SharedPrefUtils,
    protected val packageDB: PackageDB,
    protected val contactDB: ContactDB,
    val environmentHandler: EnvironmentHandler,
) : BaseViewModel() {

    /**
     * @return the json string of Backup object, and Empty string if DB is empty
     */
    fun getBackupJson(): Single<String> {
        return Single.zip(
            packageDB.getTableJson(),
            contactDB.getTableJson(),
            { t1, t2 ->
                if (t1.third && t2.third) {
                    return@zip ""
                } else {
                    val jsonObject = JsonObject()
                    //add tables here
                    jsonObject.add(t1.first, t1.second)
                    jsonObject.add(t2.first, t2.second)

                    jsonObject.toString()
                }
            })
    }

    fun getEnvironment(): Env {
        return environmentHandler.currentEnv
    }

    fun importDbData(context: Context): Completable {
        return sharedPrefUtils.importData(context)
    }
}