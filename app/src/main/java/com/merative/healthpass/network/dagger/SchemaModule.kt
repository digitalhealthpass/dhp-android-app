package com.merative.healthpass.network.dagger

import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.repository.SchemaRepo
import com.merative.healthpass.network.serviceinterface.SchemaService
import com.merative.healthpass.utils.pref.IssuerMetadataDB
import com.merative.healthpass.utils.pref.SchemaDB
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class SchemaModule {
    @Provides
    fun provideSchemaRepo(
        schemaService: SchemaService,
        schemaDB: SchemaDB,
        metadataDB: IssuerMetadataDB
    ): SchemaRepo {
        return SchemaRepo(schemaService, schemaDB, metadataDB)
    }

    @Provides
    fun provideSchemaService(@RegularApi retrofit: Retrofit): SchemaService {
        return retrofit
            .create(SchemaService::class.java)
    }
}