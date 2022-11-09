package com.merative.healthpass.ui.pin

import com.merative.healthpass.models.pin.PinCode
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class PinViewModel @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
    private val packageDB: PackageDB,
    private val contactDB: ContactDB
) : BaseViewModel() {

    fun getPin(): PinCode = sharedPrefUtils.getPin()

    fun savePin(pinCode: PinCode): Completable {
        return Completable.fromCallable {
            sharedPrefUtils.putString(SharedPrefUtils.KEY_PIN, pinCode.stringfy(), true)
        }
    }

    fun resetDB(): Completable {
        return packageDB.deleteAll()
            .andThen(contactDB.deleteAll())
            .andThen {
                sharedPrefUtils.putString(SharedPrefUtils.KEY_PIN, PinCode.SKIPPED.stringfy(), true)
                it.onComplete()
            }
    }
}