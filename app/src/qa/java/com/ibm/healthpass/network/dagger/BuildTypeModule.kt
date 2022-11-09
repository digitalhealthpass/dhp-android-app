package com.merative.healthpass.network.dagger

import dagger.Module
import dagger.Provides
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion

@Module
class BuildTypeModule {

    @Provides
    fun provideConnectionSpec(): ConnectionSpec? {
        return null
    }
}