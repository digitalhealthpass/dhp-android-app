package com.merative.healthpass.utils.pref

import com.merative.watson.healthpass.verificationengine.models.issuer.IssuerKey

class DccIssuerDB(sharedPrefUtils: SharedPrefUtils) :
    BaseDB<IssuerKey>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    companion object {
        const val VERSION = 1
        const val DB_NAME = "dcc-pk"
    }
}