package com.merative.healthpass.utils.pref

import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.ContactPackage
import io.reactivex.rxjava3.core.Maybe

@Deprecated("The key will be saved in the ContactPackage. This may be need for Contacts for versions 103 and before")
/**
 * The key will be saved in the ContactPackage. This may be need for Contacts for versions 102 and before
 *
 * Also check [ContactKeyDBMigration]
 */
class AsymmetricKeyDB(sharedPrefUtils: SharedPrefUtils) : BaseDB<AsymmetricKey>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    /**
     * It will emit the value if found or NullPointerException if is No key.
     */
    @Deprecated("the key will be saved in the contact package itself, this is used for backward compatibility for now")
    fun loadKeyForContact(contactPackage: ContactPackage): Maybe<AsymmetricKey> {
        if (contactPackage.asymmetricKey != null) {
            //the key is added in contacts DB v1
            return Maybe.just(contactPackage.asymmetricKey)
        }

        return loadAll()
            .flatMapMaybe { loadedValue ->
                val credPublicKey = contactPackage.getIdCredSubjectKey()

                val foundKey = loadedValue.dataList.firstOrNull { it.publicKey == credPublicKey }
                if (foundKey != null) {
                    Maybe.just(foundKey)
                } else {
                    Maybe.empty()
                }
            }
    }

    companion object {
        const val VERSION = 0
        const val DB_NAME = "AsymmetricKey"
    }
}