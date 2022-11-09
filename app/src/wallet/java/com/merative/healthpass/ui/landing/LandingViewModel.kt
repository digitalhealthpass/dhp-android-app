package com.merative.healthpass.ui.landing

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.IssuerMetadataDB
import com.merative.healthpass.utils.pref.SchemaDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class LandingViewModel @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils,
    private val environmentHandler: EnvironmentHandler,
    private val contactDB: ContactDB,
    private val schemaDB: SchemaDB,
    private val metadataDB: IssuerMetadataDB,
) : ViewModel() {

    var issuersLoaded: Boolean = false

    fun isTermsAccepted(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_TERMS_ACCEPTED)
    }

    fun isPrivacyAccepted(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_PRIVACY_ACCEPTED)
    }

    fun canShowTutorial(): Boolean {
        return !sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_TUTORIAL_SHOWN)
    }

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()

    fun hasPin(): Boolean = sharedPrefUtils.hasPin()

    fun hasPinSkipped(): Boolean = sharedPrefUtils.hasPinSkipped()

    fun isTimeToResetCache(): Boolean {
        val autoReset = sharedPrefUtils.getAutoReset()
        if (autoReset.isEnabled and autoReset.isTimeToReset()) {
            return true
        }
        return false
    }

    fun resetCache(): Completable {
        return schemaDB.deleteAll()
            .andThen(metadataDB.deleteAll())
    }

    fun isEnvRegionSet(): Boolean = environmentHandler.isEnvRegionSet()

    fun loadContact(profileCredId: String): Maybe<ContactPackage> {
        return contactDB.loadAll().map { it.dataList }
            .flatMapMaybe { contactPackageList ->
                Maybe.create<ContactPackage> { emitter ->
                    val contactPackage = contactPackageList.firstOrNull { contactPackage ->
                        contactPackage.profilePackage?.verifiableObject?.credential?.id?.contains(profileCredId) == true
                    }

                    if (contactPackage != null) {
                        emitter.onSuccess(contactPackage)
                    } else {
                        emitter.onComplete()
                    }
                }
            }
    }
}