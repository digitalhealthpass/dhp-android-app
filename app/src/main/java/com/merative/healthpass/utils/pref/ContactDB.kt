package com.merative.healthpass.utils.pref

import com.merative.healthpass.models.sharedPref.ContactPackage

class ContactDB(sharedPrefUtils: SharedPrefUtils) : BaseDB<ContactPackage>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    companion object {
        const val VERSION = 1
        const val DB_NAME = "contacts"
    }

    //v1 merging key db with contacts db
}