package com.merative.healthpass.common.dagger

import android.content.Context
import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.network.serviceinterface.IssuerService
import com.merative.healthpass.utils.pref.DccIssuerDB
import com.merative.healthpass.utils.pref.IssuerDB
import com.merative.healthpass.utils.pref.ShcIssuerDB
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class FlavorModule {
    @Provides
    fun provideAppFlavorActions(context: Context): AppFlavorActions {
        return AppFlavorActions(context)
    }

    @Provides
    fun provideIssuerRepo(
        issuerService: IssuerService,
        issuerDB: IssuerDB,
        shcIssuerDB: ShcIssuerDB,
        dccIssuerDB: DccIssuerDB
    ): IssuerRepo {
        return IssuerRepo(issuerService, issuerDB, shcIssuerDB, dccIssuerDB)
    }

    @Provides
    fun provideIssuerService(@RegularApi retrofit: Retrofit): IssuerService {
        return retrofit.create(IssuerService::class.java)
    }
}