package com.merative.healthpass.ui.settings.reset

import android.content.Context
import com.merative.healthpass.R
import com.merative.healthpass.models.sharedPref.AutoReset
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

class ResetCacheViewModel @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
    private val packageDB: PackageDB,
    private val contactDB: ContactDB,
    private val schemaDB: SchemaDB,
    private val metadataDB: IssuerMetadataDB,
    private val issuerDB: IssuerDB,
    private val shcIssuerDB: ShcIssuerDB,
    private val dccIssuerDB: DccIssuerDB
) : BaseViewModel() {

    private val autoReset: AutoReset = sharedPrefUtils.getAutoReset()

    fun deleteAllSchema(): Completable {
        return schemaDB.deleteAll()
    }

    fun deleteAllMetadata(): Completable {
        return metadataDB.deleteAll()
    }

    fun resetCache(): Completable {
        return metadataDB.deleteAll()
            .andThen(schemaDB.deleteAll())
            .andThen(issuerDB.deleteAll())
            .andThen(dccIssuerDB.deleteAll())
            .andThen(shcIssuerDB.deleteAll())
    }

    fun setAutoResetFrequencyEnabled(
        isEnabled: Boolean,
        frequency: Int = 0,
        lastDate: Calendar? = null
    ): Completable {
        return Completable.fromCallable {
            autoReset.isEnabled = isEnabled
            autoReset.frequency = frequency
            lastDate?.let {
                autoReset.lastAutoResetDate = lastDate
            }
            sharedPrefUtils.setAutoReset(autoReset)
        }
    }

    fun getAutoResetFrequencyEnabled(): Boolean {
        return autoReset.isEnabled
    }

    fun getLastResetDate(context: Context): String {
        return if (getAutoResetFrequencyEnabled()) {
            autoReset.getFormattedLastAutoResetDate()
        } else {
            context.getString(R.string.profile_never)
        }
    }

    fun getAutoResetFrequency(): Int {
        return autoReset.frequency
    }

    //Returns size in Bytes
    fun getSize(): Long {
        return schemaDB.getSize() + metadataDB.getSize() + issuerDB.getSize() + shcIssuerDB.getSize() + dccIssuerDB.getSize()
    }

    fun loadAllCredentialsCount(): Single<Pair<Int, Int>> {
        return Single.zip(
            packageDB.loadAll(),
            contactDB.loadAll(),
            { t1, t2 ->
                t1.dataList.size to t2.dataList.size
            })
    }

    fun deleteAllCredentialsAndSchema(): Completable {
        return packageDB.deleteAll()
    }
}