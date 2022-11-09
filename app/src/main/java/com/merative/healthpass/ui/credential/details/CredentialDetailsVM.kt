package com.merative.healthpass.ui.credential.details

import com.merative.healthpass.exception.UnknownIssuerException
import com.merative.healthpass.extensions.loge
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.models.DccIssuerMetaData
import com.merative.healthpass.models.IssuerMetaData
import com.merative.healthpass.models.ShcIssuerMetaData
import com.merative.healthpass.models.VcIssuerMetaData
import com.merative.healthpass.models.region.Env
import com.merative.healthpass.models.results.*
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.network.repository.IssuerRepo
import com.merative.healthpass.network.repository.RevocationRepo
import com.merative.healthpass.network.repository.SamsungRepo
import com.merative.healthpass.network.repository.SchemaRepo
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.Utils
import com.merative.healthpass.utils.pref.ContactDB
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.toSingle
import com.merative.healthpass.utils.toSingleAndEnsure
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.exception.VerifyError
import com.merative.watson.healthpass.verificationengine.rules.RulesException
import com.merative.watson.healthpass.verificationengine.signature.hasValidSignature
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class CredentialDetailsVM @Inject constructor(
    private val packageDB: PackageDB,
    private val schemaRepo: SchemaRepo,
    private val contactDB: ContactDB,
    private val issuerRepo: IssuerRepo,
    private val revocationRepo: RevocationRepo,
    private val samsungRepo: SamsungRepo,
    private val environmentHandler: EnvironmentHandler,
) : BaseViewModel() {

    fun loadConnections(): Single<List<ContactPackage>> {
        return contactDB.loadAll()
            .map { it.dataList }
    }

    fun updateCredential(aPackage: Package): Single<Boolean> {
        return schemaRepo.getSchemaAndMetaData(aPackage)
            .flatMap {
                var toSave: Package? = null
                if (it.first.isSuccessful && it.first.body()?.payload != null) {
                    toSave = aPackage.copy(schema = it.first.body()?.payload)
                }
                if (it.second.isSuccessful && it.second.body()?.payload != null) {
                    toSave = aPackage.copy(issuerMetaData = it.second.body()?.payload)
                }

                if (toSave != null) {
                    packageDB.insert(toSave, true)
                } else {
                    Single.just(false)
                }
            }
    }

    fun validate(model: VerificationResultsModel): Single<VerificationResultsModel> {
        model.verifyEngine = VerifyEngine(model.verifiableObject!!)
        return Single.zip(verifySignature(model), model.verifiableObject?.let { isNotRevoked(it) },
            { res1, res2 ->
                res1.isNotRevoked = res2
                res1
            }
        )
    }

    private fun isNotRevoked(verifiableObject: VerifiableObject): Single<Boolean> {
        return when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> {
                revocationRepo.getRevokeStatus(verifiableObject.credential!!.id)
            }
            else -> Single.just(true)
        }
    }

    private fun verifySignature(model: VerificationResultsModel): Single<VerificationResultsModel> {
        val verifyEngine = model.verifyEngine!!
        return issuerRepo.getIssuerFromDb(verifyEngine.verifiableObject!!)
            .map { parsePublicKeyResponse(it, verifyEngine) }
            .map { if (noKeysAvailable(verifyEngine)) throw UnknownIssuerException() }
            .flatMap { verifyEngine.hasValidSignature().toSingleAndEnsure { it } }
            .flatMap { valid ->
                model.isSignVerified = valid
                model.result = if (valid) CredentialValid() else getVerificationFailedResult()
                model.toSingle()
            }
            .onErrorResumeNext {
                model.isSignVerified = false
                model.result = getVerificationFailedResult(it)
                model.toSingle()
            }
    }

    private fun noKeysAvailable(verifyEngine: VerifyEngine) =
        verifyEngine.issuerKeys?.isEmpty().orValue(true) &&
                verifyEngine.jwkSet?.isEmpty().orValue(true) &&
                verifyEngine.issuer == null

    private fun parsePublicKeyResponse(issuerMetaData: IssuerMetaData, verifyEngine: VerifyEngine) {
        when (issuerMetaData) {
            is VcIssuerMetaData -> verifyEngine.issuer = issuerMetaData.issuer
            is ShcIssuerMetaData -> verifyEngine.jwkSet = issuerMetaData.jwkList
            is DccIssuerMetaData -> verifyEngine.issuerKeys = issuerMetaData.issuerKeysList
        }
    }

    private fun getVerificationFailedResult(throwable: Throwable? = null) =
        when (throwable) {
            is RulesException -> CredentialRulesInvalid()
            is VerifyError -> InvalidSignature()
            is UnknownIssuerException -> UnknownIssuer()
            else -> UnknownVerificationError().also { loge("UnknownVerificationError", throwable) }
        }

    fun deleteCredential(aPackage: Package): Completable {
        return packageDB.delete(aPackage)
    }

    fun getEnvironment(): Env {
        return environmentHandler.currentEnv
    }

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()

    fun getCountryCode(): String? {
        return Utils.WalletDeviceUtils.getCountryIsoCode()
    }

    fun getModelName(): String? {
        return Utils.WalletDeviceUtils.getModelName()
    }

    suspend fun checkDeviceAvailSamsungPay(countryCode: String, modelName: String, serviceType: String): Boolean {
        return samsungRepo.checkDeviceAvailSamsungPay(countryCode, modelName, serviceType)
    }

}