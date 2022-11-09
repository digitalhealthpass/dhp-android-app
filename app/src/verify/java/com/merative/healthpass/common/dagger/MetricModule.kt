package com.merative.healthpass.common.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.MetricRepo
import com.merative.healthpass.network.serviceinterface.MetricService
import com.merative.healthpass.utils.pref.IssuerDB
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MetricModule {
    @Provides
    fun provideMetricRepo(
        metricService: MetricService
    ): MetricRepo {
        return MetricRepo(metricService)
    }

    @Provides
    fun provideMetricService(@RegularApi retrofit: Retrofit): MetricService {
        return retrofit
            .create(MetricService::class.java)
    }
}