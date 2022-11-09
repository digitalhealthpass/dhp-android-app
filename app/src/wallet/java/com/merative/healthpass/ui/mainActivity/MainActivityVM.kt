package com.merative.healthpass.ui.mainActivity

import androidx.lifecycle.ViewModel
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class MainActivityVM @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
    private val environmentHandler: EnvironmentHandler,
    private val contactDB: ContactDB,
) : ViewModel() {
    var isPinValidationRequired = true
        private set

    init {
        if (hasPinSkipped())
            isPinValidationRequired = false
    }

    fun validationFinished() {
        isPinValidationRequired = false
    }

    fun reset() {
        isPinValidationRequired = true
    }

    private fun hasPinSkipped(): Boolean = sharedPrefUtils.hasPinSkipped()

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

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()
}