package com.merative.healthpass.common.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.RevocationRepo
import com.merative.healthpass.network.serviceinterface.RevocationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class RevocationModule {
    @Provides
    fun provideRevocationRepo(
        metricService: RevocationService
    ): RevocationRepo {
        return RevocationRepo(metricService)
    }

    @Provides
    fun provideRevocationService(@RegularApi retrofit: Retrofit): RevocationService {
        return retrofit
            .create(RevocationService::class.java)
    }
}