package com.merative.healthpass.common.dagger

import android.content.Context
import com.merative.healthpass.common.App
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.Utils
import com.merative.healthpass.utils.pref.SharedPrefUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: App) {

    @Singleton
    @Provides
    fun providesApplication(): Context {
        return application
    }

    @Singleton
    @Provides
    fun provideEnvironmentHandler(sharedPrefUtils: SharedPrefUtils): EnvironmentHandler {
        return EnvironmentHandler(sharedPrefUtils)
    }

    @Provides
    fun provideSharedPref(): SharedPrefUtils {
        return SharedPrefUtils(application)
    }

    @Provides
    fun provideUtils(): Utils {
        return Utils()
    }
}