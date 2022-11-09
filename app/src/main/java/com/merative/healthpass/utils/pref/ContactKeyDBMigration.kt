package com.merative.healthpass.utils.pref

import com.merative.healthpass.extensions.logi
import com.merative.healthpass.extensions.logw
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.ContactPackage
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.MaybeSource
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class ContactKeyDBMigration @Inject constructor(
    private val contactDB: ContactDB,
    private val asymmetricKeyDB: AsymmetricKeyDB,
) {

    fun start() {
        contactDB.loadAll()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .zipWith(
                asymmetricKeyDB.loadAll(),
                { t1, t2 -> t1 to t2 }
            )
            .flatMapMaybe { mergeContactAndKey(it) }
            .subscribe({
                logi("migrated data from key db to contacts db: $it")
            }, rxError("couldn't migrate"), {
                logi("there was no data in key DB to migrate to contact DB")
            })
    }

    private fun mergeContactAndKey(pair: Pair<Table<ContactPackage>, Table<AsymmetricKey>>): Maybe<Boolean> {
        val contactsTable = pair.first
        val keysTable: Table<AsymmetricKey> = pair.second

        val contactsModified = ArrayList<ContactPackage>()
        val keysModified = ArrayList<AsymmetricKey>()

        if (keysTable.dataList.isNotEmpty()) {

            contactsTable.dataList.forEachIndexed { _, contactPackage ->
                val credPublicKey = contactPackage.getIdCredSubjectKey()

                val foundKey = keysTable.dataList.firstOrNull { it.publicKey == credPublicKey }
                if (foundKey != null) {
                    keysModified.add(foundKey)

                    val copy = contactPackage.copy(asymmetricKey = foundKey)
                    contactsModified.add(copy)
                }
            }
        }

        if (contactsModified.isNotEmpty()) {
            return contactDB.insertAll(contactsModified, true)
                .ignoreElement()
                .andThen(
                    asymmetricKeyDB.deleteList(keysModified)
                ).andThen(MaybeSource {
                    logw("keys left ${keysTable.dataList.size - keysModified.size} ")
                    it.onSuccess(true)
                })
        }

        logw("keys left ${keysTable.dataList.size - keysModified.size} ")

        return Maybe.empty()
    }
}