package com.merative.healthpass.utils.pref

import com.merative.healthpass.models.api.ConfigResponse

class ConfigDB(sharedPrefUtils: SharedPrefUtils) :
    BaseDB<ConfigResponse>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    companion object {
        const val VERSION = 1
        const val DB_NAME = "rules-config"
    }
}