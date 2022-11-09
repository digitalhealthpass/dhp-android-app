package com.merative.healthpass.network.repository

import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.models.api.metadata.MetaResponse
import com.merative.healthpass.models.api.schema.SchemaResponse
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.serviceinterface.SchemaService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.BaseDB
import com.merative.healthpass.utils.pref.IssuerMetadataDB
import com.merative.healthpass.utils.pref.SchemaDB
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class SchemaRepo @Inject constructor(
    private val schemaService: SchemaService,
    private val schemaDB: SchemaDB,
    private val metadataDB: IssuerMetadataDB
) {
    fun getSchemaAndMetaData(
        aPackage: Package
    ): Single<Pair<Response<SchemaResponse>, Response<MetaResponse>>> {
        return Single.zip(
            fetchSchema(aPackage.verifiableObject.credential!!),
            //this added because we don't need to track errors for metadata in this case, return internal response
            fetchMetaData(aPackage.verifiableObject.credential!!),
            { schemaResponse, metadataResponse ->
                schemaResponse to metadataResponse
            }
        ).asyncToUiSingle()
    }

    /**
     * Get schema from DB if exists, or request it online
     */
    fun fetchSchema(credential: Credential): Single<Response<SchemaResponse>> {
        return fetchSchema(credential.credentialSchema?.id.toString())
    }

    /**
     * Get schema from DB if exists, or request it online
     */
    fun fetchSchema(did: String): Single<Response<SchemaResponse>> {
        return getSchemaFromDB(did)
            .flatMap {
                if (it.isPresent) {
                    Single.just(Response.success(it.get()))
                } else {
                    schemaService.getSchema(did)
                        .flatMap { response ->
                            if (response.isSuccessfulAndHasBody()) {
                                insertToDB(schemaDB, response.body()!!).map { response }
                            } else {
                                Single.just(response)
                            }
                        }
                }
            }
    }//schema

    /**
     * Get metadata from DB if exist, or request it online
     */
    fun fetchMetaData(credential: Credential): Single<Response<MetaResponse>> {
        return getMetadataFromDB(credential.issuer.toString())
            .flatMap {
                if (it.isPresent) {
                    Single.just(Response.success(it.get()))
                } else {
                    schemaService.requestMetadata(credential.issuer.orEmpty())
                        .flatMap { response ->
                            if (response.isSuccessfulAndHasBody()) {
                                insertToDB(metadataDB, response.body()!!).map { response }
                            } else {
                                Single.just(response)
                            }
                        }
                        .onErrorResumeNext { error ->
                            RxHelper.handleErrorSingle(error)
                        }
                }
            }
    }

    private fun getSchemaFromDB(schemaID: String): Single<Optional<SchemaResponse>> {
        return schemaDB.loadAll()
            .map { schemaTable ->
                val schemaFound = schemaTable.dataList.firstOrNull {
                    it.payload?.id == schemaID
                }
                Optional.ofNullable(schemaFound)

            }
    }

    private fun getMetadataFromDB(issuer: String): Single<Optional<MetaResponse>> {
        return metadataDB.loadAll()
            .map { metaTable ->
                val metaFound = metaTable.dataList.firstOrNull {
                    it.payload.id == issuer
                }
                Optional.ofNullable(metaFound)

            }
    }

    private fun <Model> insertToDB(database: BaseDB<Model>, model: Model): Single<Boolean> {
        return database.insert(model, true)
    }
}