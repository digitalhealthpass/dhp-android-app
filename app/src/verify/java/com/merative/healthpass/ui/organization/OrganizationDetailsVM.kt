package com.merative.healthpass.ui.organization

import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.models.OrganizationException
import com.merative.healthpass.models.api.login.LoginResponse
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.ConfigRepo
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.network.repository.LoginRepo
import com.merative.healthpass.network.repository.SchemaRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.watson.healthpass.verifiablecredential.models.credential.getConfigId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getConfigVersion
import com.merative.watson.healthpass.verifiablecredential.models.credential.isNotValid
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class OrganizationDetailsVM @Inject constructor(
    private val packageDB: PackageDB,
    private val loginRepo: LoginRepo,
    private val schemaRepo: SchemaRepo,
    private val configRepo: ConfigRepo,
    private val issuerRepo: IssuerRepo
) : BaseViewModel() {

    fun loginWithOrgCredential(
        newPackage: Package,
        currentSelectedCP: Package?,
        firstTime: Boolean,
    ): Single<Package> {
        return Single.fromCallable {
            if (newPackage.verifiableObject.credential?.isNotValid().orValue(true)) {
                throw OrganizationException("credential is invalid")
            }
            newPackage
        }.flatMap {
            loginRepo.loginWithCredential(newPackage)
        }.flatMap { response ->
            handleResponse(
                response,
                newPackage,
                currentSelectedCP,
                firstTime
            )
        }
    }

    private fun handleResponse(
        response: Response<LoginResponse>,
        newPackage: Package,
        currentSelectedCP: Package?,
        firstTime: Boolean,
    ): Single<Package> {
        return if (response.isSuccessfulAndHasBody()) {
            if (firstTime) {
                requestSchemaAndCredential(newPackage, currentSelectedCP)
            } else {
                saveSelectedOrg(newPackage, currentSelectedCP)
            }
        } else {
            throw HttpException(response)
        }
    }

    private fun saveSelectedOrg(
        newPackage: Package,
        currentSelectedCP: Package?,
    ): Single<Package> {
        val list = arrayListOf(newPackage)
        //order is important
        if (currentSelectedCP != null && currentSelectedCP != newPackage) {
            currentSelectedCP.isSelected = false
            list.add(currentSelectedCP)
        }
        newPackage.isSelected = true
        val id = newPackage.verifiableObject.credential?.getConfigId()
            ?: throw OrganizationException("cannot find config ID")
        val version = newPackage.verifiableObject.credential?.getConfigVersion()
            ?: "latest" //throw OrganizationException("cannot find config version")
        return configRepo.loadAndInsertConfig(id, version)
            .flatMap { packageDB.insertAll(list, true) }
            .flatMap { issuerRepo.loadAndSaveSignatureKeys() }
            .flatMap { Single.just(newPackage) }
    }

    fun deleteOrganization(aPackage: Package): Completable {
        return packageDB.delete(aPackage)
    }

    private fun requestSchemaAndCredential(
        newPackage: Package,
        currentSelectedCP: Package?,
    ): Single<Package> {
        return schemaRepo.getSchemaAndMetaData(newPackage)
            .map {
                if (!it.first.isSuccessful) {
                    throw HttpException(it.first)
                }
                newPackage.copy(
                    schema = it.first.body()?.payload,
                    issuerMetaData = it.second.body()?.payload
                )
            }.asyncToUiSingle()
            .flatMap { updatedCP ->
                saveSelectedOrg(updatedCP, currentSelectedCP).flatMap { Single.just(updatedCP) }
            }
            .flatMap { updatedCP ->
                issuerRepo.loadAndSaveSignatureKeys().flatMap { Single.just(updatedCP) }
            }
    }

}