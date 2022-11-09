package com.merative.healthpass.ui.settings.pinSettings

import com.merative.healthpass.models.pin.PinCode
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class PinSettingsViewModel @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils
) : BaseViewModel() {

    fun getPin(): PinCode = sharedPrefUtils.getPin()

    fun hasPin(): Boolean = sharedPrefUtils.hasPin()

    fun deletePin(): Completable {
        return Completable.fromCallable {
            sharedPrefUtils.putString(SharedPrefUtils.KEY_PIN, PinCode.SKIPPED.stringfy())
        }
    }
}