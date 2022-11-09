package com.merative.healthpass.network.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.network.repository.UDCredentialsRepo
import com.merative.healthpass.network.serviceinterface.NihRegistrationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class NihRegistrationModule {

    @Provides
    fun provideNihRegistrationRepo(
        nihRegistrationService: NihRegistrationService,
    ): NIHRegRepo {
        return NIHRegRepo(nihRegistrationService)
    }

    @Provides
    fun provideRegistrationService(@RegularApi retrofit: Retrofit): NihRegistrationService {
        return retrofit
            .create(NihRegistrationService::class.java)
    }

    @Provides
    fun provideUDRepo(
        service: NihRegistrationService,
    ): UDCredentialsRepo {
        return UDCredentialsRepo(service)
    }
}