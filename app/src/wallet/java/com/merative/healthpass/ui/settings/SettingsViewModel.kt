package com.merative.healthpass.ui.settings

import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    sharedPrefUtils: SharedPrefUtils,
    packageDB: PackageDB,
    contactDB: ContactDB,
    environmentHandler: EnvironmentHandler,
    private val issuerRepo: IssuerRepo
) : BaseSettingsViewModel(
    sharedPrefUtils, packageDB, contactDB, environmentHandler
) {

    fun loadIssuers(): Completable {
        return issuerRepo.resetIssuerInformation()
    }
}