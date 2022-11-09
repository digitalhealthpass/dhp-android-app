package com.merative.healthpass.network.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.NCLRegRepo
import com.merative.healthpass.network.serviceinterface.NclRegistrationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class NclRegistrationModule {

    @Provides
    fun provideNclRegistrationRepo(
        nclRegistrationService: NclRegistrationService,
    ): NCLRegRepo {
        return NCLRegRepo(nclRegistrationService)
    }

    @Provides
    fun provideRegistrationService(@RegularApi retrofit: Retrofit): NclRegistrationService {
        return retrofit
            .create(NclRegistrationService::class.java)
    }
}