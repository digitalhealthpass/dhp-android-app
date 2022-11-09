package com.merative.healthpass.network.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.HitRepo
import com.merative.healthpass.network.serviceinterface.HitRegistrationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class HitRegistrationModule {

    @Provides
    fun provideNihRegistrationRepo(service: HitRegistrationService): HitRepo {
        return HitRepo(service)
    }

    @Provides
    fun provideRegistrationService(@RegularApi retrofit: Retrofit): HitRegistrationService {
        return retrofit
            .create(HitRegistrationService::class.java)
    }
}