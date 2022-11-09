package com.merative.healthpass.common.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.ConfigRepo
import com.merative.healthpass.network.serviceinterface.ConfigService
import com.merative.healthpass.utils.pref.ConfigDB
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class ConfigModule {

    @Provides
    fun provideConfigRepo(
        configService: ConfigService,
        configDb: ConfigDB
    ): ConfigRepo {
        return ConfigRepo(configService, configDb)
    }

    @Provides
    fun provideConfigService(@RegularApi retrofit: Retrofit): ConfigService {
        return retrofit
            .create(ConfigService::class.java)
    }
}