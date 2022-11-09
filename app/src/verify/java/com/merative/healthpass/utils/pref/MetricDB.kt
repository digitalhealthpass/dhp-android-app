package com.merative.healthpass.utils.pref

import com.merative.healthpass.models.Metric

class MetricDB(sharedPrefUtils: SharedPrefUtils) : BaseDB<Metric>(sharedPrefUtils) {
    override val dbName: String
        get() = DB_NAME
    override val version: Int
        get() = VERSION

    companion object {
        const val VERSION = 1
        const val DB_NAME = "metric"
    }
}