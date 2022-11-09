package com.merative.healthpass.common.dagger

import com.merative.healthpass.utils.pref.*
import dagger.Module
import dagger.Provides

@Module
class DBModule {

    @Provides
    fun providePackageDB(sharedPrefUtils: SharedPrefUtils): PackageDB {
        return PackageDB(sharedPrefUtils)
    }

    @Provides
    fun provideContactDB(sharedPrefUtils: SharedPrefUtils): ContactDB {
        return ContactDB(sharedPrefUtils)
    }

    @Provides
    fun provideAsymmetricDB(sharedPrefUtils: SharedPrefUtils): AsymmetricKeyDB {
        return AsymmetricKeyDB(sharedPrefUtils)
    }

    @Provides
    fun provideSchemaDB(sharedPrefUtils: SharedPrefUtils): SchemaDB {
        return SchemaDB(sharedPrefUtils)
    }

    @Provides
    fun provideMetadataDB(sharedPrefUtils: SharedPrefUtils): IssuerMetadataDB {
        return IssuerMetadataDB(sharedPrefUtils)
    }

    @Provides
    fun provideIssuerDB(sharedPrefUtils: SharedPrefUtils): IssuerDB {
        return IssuerDB(sharedPrefUtils)
    }

    @Provides
    fun provideShcIssuerDB(sharedPrefUtils: SharedPrefUtils): ShcIssuerDB {
        return ShcIssuerDB(sharedPrefUtils)
    }

    @Provides
    fun provideDccIssuerDB(sharedPrefUtils: SharedPrefUtils): DccIssuerDB {
        return DccIssuerDB(sharedPrefUtils)
    }

    @Provides
    fun provideMetricDB(sharedPrefUtils: SharedPrefUtils): MetricDB {
        return MetricDB(sharedPrefUtils)
    }

    @Provides
    fun provideConfigDB(sharedPrefUtils: SharedPrefUtils): ConfigDB {
        return ConfigDB(sharedPrefUtils)
    }
}