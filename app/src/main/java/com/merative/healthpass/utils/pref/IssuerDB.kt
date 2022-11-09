package com.merative.healthpass.utils.pref

import com.merative.watson.healthpass.verificationengine.models.issuer.Issuer

class IssuerDB(sharedPrefUtils: SharedPrefUtils) :
    BaseDB<Issuer>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    companion object {
        const val VERSION = 1
        const val DB_NAME = "issuer"
    }
}