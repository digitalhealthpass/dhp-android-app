package com.merative.healthpass.common

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.merative.healthpass.common.dagger.AppModule
import com.merative.healthpass.common.dagger.ApplicationComponent
import com.merative.healthpass.common.dagger.DaggerApplicationComponent
import com.merative.healthpass.extensions.IS_QA
import com.merative.healthpass.network.dagger.NetworkModule

class App : MultiDexApplication() {
    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        context = this
        appComponent = DaggerApplicationComponent
            .builder()
            .appModule(AppModule((this)))
            .networkModule(NetworkModule())
            .build()

        //enable only if it is dev mode, since we want to disable for Release builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(IS_QA)
        AppConstants.adjustValues(this)
        startAppComponentFeatures()
    }

    private fun startAppComponentFeatures() {
        appComponent.packageDB().migrateOldData()
        appComponent.dbMigration().start()
        appComponent.utils().clearSharedFolder(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        lateinit var context: Context
    }
}