package com.merative.healthpass.ui.results

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.merative.healthpass.common.VerifierConstants
import com.merative.healthpass.exception.ConfigurationException
import com.merative.healthpass.exception.RevocationException
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.*
import com.merative.healthpass.models.api.ConfigPayload
import com.merative.healthpass.models.metrics.MetricsPayload
import com.merative.healthpass.models.results.*
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.models.specificationconfiguration.Metrics
import com.merative.healthpass.models.specificationconfiguration.Rules
import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration
import com.merative.healthpass.network.repository.*
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.ensure
import com.merative.healthpass.utils.metrics.MetricsPathProcessor
import com.merative.healthpass.utils.pref.MetricDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.healthpass.utils.pref.Table
import com.merative.healthpass.utils.toSingle
import com.merative.healthpass.utils.toSingleAndEnsure
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.extensions.toMap
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.credential.getConfigId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getCustomerId
import com.merative.watson.healthpass.verifiablecredential.models.credential.getOrganizationId
import com.merative.watson.healthpass.verifiablecredential.models.credential.isNotValid
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.typeForDisplay
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.known.UnKnownTypeException
import com.merative.watson.healthpass.verificationengine.known.isKnown
import com.merative.watson.healthpass.verificationengine.models.rule.RulePayload
import com.merative.watson.healthpass.verificationengine.rules.RulesException
import com.merative.watson.healthpass.verificationengine.rules.doesMatchClassifierRules
import com.merative.watson.healthpass.verificationengine.rules.doesMatchRules
import com.merative.watson.healthpass.verificationengine.signature.hasValidSignature
import com.merative.watson.healthpass.verificationengine.trusted.isLegacyTrusted
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScanResultsViewModel @Inject constructor(
    private val issuerRepo: IssuerRepo,
    private val displayFieldsRepo: DisplayFieldsRepo,
    private val metricDB: MetricDB,
    private val metricRepo: MetricRepo,
    private val issuerMetaDataRepo: IssuerMetaDataRepo,
    private val configRepo: ConfigRepo,
    private val revocationRepo: RevocationRepo,
    private val sharedPrefUtils: SharedPrefUtils,
) : ViewModel() {
    private lateinit var selectedCredential: Package
    private lateinit var verifiableObject: VerifiableObject
    private lateinit var verifiableEngine: VerifyEngine
    private var metricsStatus: MetricsStatus = MetricsStatus.UNKNOWN

    // todo: remove when Verifier Config is stable
//    private var configJson: String = context.loadAssetsFileAsString(CONFIG_JSON)
    private var disposables = CompositeDisposable()
    private var configType: String? = null
    private var configPayload: ConfigPayload? = null

    private val rulesPayloadList = mutableListOf<RulePayload>()
    private val metricsPayloadList = mutableListOf<MetricsPayload>()
    private var specificationConfiguration: SpecificationConfiguration? = null

    fun init(selectedCredential: Package, credentialJson: String) {
        this.selectedCredential = selectedCredential
        verifiableObject = VerifiableObject(credentialJson)
        verifiableEngine = VerifyEngine(verifiableObject)
    }

    fun getType(): String = verifiableObject.typeForDisplay

    fun validate(isNetworkAvailAble: Boolean): Single<ResultsModel> {
        return loadConfig().flatMap { _ ->
            if (configPayload?.configuration != null) {
                loadConfig()
                    .flatMap { isAllowedInLegacyConfiguration() }
                    .flatMap { isKnown() }
                    .flatMap { verifiableEngine.isLegacyTrusted().toSingleAndEnsure { it } }
                    //  todo: Expiration check temporary disabled
                    //  .flatMap { verifiableObject.isExpired().toSingleAndEnsure { it } }
                    .flatMap { verifyLegacySignature() }
                    .flatMap {
                        if (isNetworkAvailAble) {
                            isLegacyNotRevoked().ensure(RevocationException("Credential revoked")) { it }
                        } else {
                            Single.just(true)
                        }
                    }
                    .flatMap { doesMatchLegacyRules().ensure(RulesException()) { it } }
                    .flatMap { isValid ->
                        Single.fromCallable {
                            metricsStatus =
                                if (isValid) MetricsStatus.VERIFIED else MetricsStatus.UNVERIFIED
                            isValid
                        }
                    }
                    .doOnError {
                        metricsStatus = MetricsStatus.UNVERIFIED
                    }
                    .flatMap {
                        Single.zip(
                            displayFieldsRepo.getLegacyDisplayFields(
                                verifiableObject,
                                configPayload?.configuration
                            )
                                .onErrorResumeNext { Single.just(listOf()) },
                            issuerMetaDataRepo.fetchIssuerName(verifiableObject),
                            { t1, t2 ->
                                ResultsModel(t1, t2.orElse(null), SpecificationConfiguration(), true, IOException())
                            }
                        )
                    }
                    .doFinally {
                        if (selectedCredential.verifiableObject.credential?.isNotValid() == false) {
                            createAndSubmitMetrics(isNetworkAvailAble)
                        }
                    }
            } else {
                loadConfig()
                    .flatMap { isAllowedInConfiguration() }
                    .flatMap {
                        specificationConfiguration = it
                        isKnownClassifier(it)
                    }
                    .flatMap {
                        specificationConfiguration = it
                        verifiableEngine.isTrusted(it)
                    }
                    //  todo: Expiration check temporary disabled
                    //  .flatMap { verifiableObject.isExpired().toSingleAndEnsure { it } }
                    .flatMap {
                        specificationConfiguration = it
                        verifySignature(it)
                    }
                    .flatMap { it ->
                        specificationConfiguration = it
                        if (isNetworkAvailAble) {
                            isNotRevoked(it).ensure(RevocationException("Credential revoked")) { (it.id.isNotNullOrEmpty()) }
                        } else {
                            Single.just(it)
                        }
                    }
                    .flatMap { it ->
                        specificationConfiguration = it
                        doesMatchRules(it).ensure(RulesException()) { (it.id.isNotNullOrEmpty()) }
                    }
                    .flatMap {
                        specificationConfiguration = it
                        Single.fromCallable {
                            metricsStatus =
                                if (it.id.isNotNullOrEmpty()) MetricsStatus.VERIFIED else MetricsStatus.UNVERIFIED
                            it
                        }
                    }
                    .doOnError {
                        metricsStatus = MetricsStatus.UNVERIFIED
                    }
                    .flatMap {
                        Single.zip(
                            displayFieldsRepo.getDisplayFields(
                                verifiableObject, it
                            )
                                .onErrorResumeNext { Single.just(listOf()) },
                            issuerMetaDataRepo.fetchIssuerName(verifiableObject),
                            { t1, t2 ->
                                ResultsModel(t1, t2.orElse(null), it, true, IOException())
                            }
                        )
                    }
                    .onErrorResumeNext {
                        val verificationResultModel: VerificationResult = getVerificationResult(false, null, it)
                        specificationConfiguration?.verificationResult = verificationResultModel
                        val pairs: Pair<String, String> = "" to ""
                        val list: List<Pair<String, String>> = arrayListOf()
                        listOf(pairs)
                        val model = ResultsModel(list, null, specificationConfiguration, false, it)
                        return@onErrorResumeNext model.toSingle()
                    }
                    .doFinally {
                        if (selectedCredential.verifiableObject.credential?.isNotValid() == false) {
                            createAndSubmitMetrics(isNetworkAvailAble)
                        }
                    }
            }
        }
    }

    private fun isKnown() = verifiableEngine.isKnown()
        .toSingleAndEnsure(UnKnownTypeException()) { it }

    private fun isKnownClassifier(specificationConfiguration: SpecificationConfiguration?): Single<SpecificationConfiguration> {
        var specificationConfigurations: java.util.ArrayList<SpecificationConfiguration> = configPayload?.specificationConfigurations!!
        val mapsValueSets = configPayload?.valueSets?.associateBy({ it.name }, { it -> it.items.map { it.value } })
        configPayload?.disabledSpecifications?.forEach { disabledConfiguration ->
            specificationConfigurations = specificationConfigurations.filter { it.id != disabledConfiguration.id } as ArrayList<SpecificationConfiguration>
        }
        specificationConfigurations.forEach {
            val rule = it.classifierRule?.predicate
            val res = rule?.toMap()?.let { it1 -> verifiableEngine.doesMatchClassifierRules(it1, mapsValueSets) }
            if (res != false.toString()) {
                logd("---- rules: $res; predicate: ${it.classifierRule?.predicate}")
                return it.toSingle()
            }
        }
        return (SpecificationConfiguration()).toSingleAndEnsure(UnKnownTypeException()) { false }
    }

    private fun isAllowedInLegacyConfiguration(): Single<Boolean> {
        return configPayload?.configuration?.asJsonObject?.has(verifiableObject.type.toString())
            .orValue(false)
            .toSingleAndEnsure(ConfigurationException("No configuration found for type ${verifiableObject.type.value}")) { it }
    }

    private fun isAllowedInConfiguration(): Single<SpecificationConfiguration> {
        configPayload?.specificationConfigurations?.forEach {
            if (it.credentialSpec.equals(verifiableObject.type.toString())) {
                return Single.just(it)
            }
        }
        return Single.just(SpecificationConfiguration())
    }

    private fun isLegacyNotRevoked(): Single<Boolean> {
        return when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP -> {
                revocationRepo.getRevokeStatus(verifiableObject.credential!!.id)
            }
            else -> Single.just(true)
        }
    }

    private fun isNotRevoked(specificationConfiguration: SpecificationConfiguration): Single<SpecificationConfiguration> {
        return when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP -> {
                return if (revocationRepo.getRevokeStatus(verifiableObject.credential!!.id) == true.toSingle())
                    Single.just(specificationConfiguration)
                else {
                    Single.just(specificationConfiguration)
                }
            }
            else -> Single.just(specificationConfiguration)
        }
    }

    private fun verifyLegacySignature(): Single<Boolean> {
        return issuerRepo.getIssuerFromDb(verifiableObject)
            .map { parsePublicKeyResponse(it) }
            .flatMap {
                verifiableEngine.hasValidSignature()
                    .toSingleAndEnsure(Exception("Signature Verification failed")) { it }
            }
    }

    private fun verifySignature(specificationConfiguration: SpecificationConfiguration): Single<SpecificationConfiguration> {
        return issuerRepo.getIssuerFromDb(verifiableObject)
            .map { parsePublicKeyResponse(it) }
            .flatMap {
                if (verifiableEngine.hasValidSignature())
                    return@flatMap Single.just(specificationConfiguration)
                else {
                    return@flatMap Single.just(SpecificationConfiguration())
                }
            }.onErrorResumeNext {
                specificationConfiguration.verificationResult = getVerificationResult(false, null, it)
                return@onErrorResumeNext specificationConfiguration.toSingle()
            }
    }

    private fun parsePublicKeyResponse(issuerMetaData: IssuerMetaData) {
        when (issuerMetaData) {
            is VcIssuerMetaData -> verifiableEngine.issuer = issuerMetaData.issuer
            is ShcIssuerMetaData -> verifiableEngine.jwkSet = issuerMetaData.jwkList
            is DccIssuerMetaData -> verifiableEngine.issuerKeys = issuerMetaData.issuerKeysList
        }
    }

    // region rules
    private fun doesMatchLegacyRules(): Single<Boolean> {
        return Observable.fromIterable(rulesPayloadList)
            .flatMapSingle {
                val res = it.predicate.let { it1 -> verifiableEngine.doesMatchRules(it1.toMap()) }
                if (res != true.toString()) logd("---- rules: $res; predicate: ${it.predicate}")
                res.toSingle()
            }
            .toList()
            .map {
                if (it.contains(VerifyEngine.RULES_MATCH_UNKNOWN)) {
                    verifiableObject.isRulesUnknown = true
                }
                !it.contains(VerifyEngine.RULES_MATCH_FALSE)
            }
    }

    private fun doesMatchRules(specificationConfiguration: SpecificationConfiguration): Single<SpecificationConfiguration> {
        var rules: List<Rules>? = null
        if (specificationConfiguration.rules.isNotEmpty()) {
            rules = specificationConfiguration.rules

            val disabledRules = configPayload?.disabledRules?.filter { it.specID == specificationConfiguration.id }

            if (disabledRules?.isNotEmpty() == true) {
                disabledRules.forEach { disabledRule ->
                    rules = rules?.filter { it.id != disabledRule.id } as ArrayList<Rules>
                }
            }
        }

        val mapsValueSets = configPayload?.valueSets?.associateBy({ it.name }, { it -> it.items.map { it.value } })

        return Observable.fromIterable(rules)
            .flatMapSingle {
                val res = it.predicate.let { it1 -> it1?.toMap()?.let { it2 -> verifiableEngine.doesMatchRules(it2, mapsValueSets) } }
                if (res == true.toString()) logd("---- rules: $res; predicate: ${it.predicate}")
                res.toSingle()
            }
            .toList()
            .map {
                if (it.contains(VerifyEngine.RULES_MATCH_UNKNOWN)) {
                    verifiableObject.isRulesUnknown = true
                }
                if (it.contains(VerifyEngine.RULES_MATCH_FALSE) || it.contains(VerifyEngine.RULES_MATCH_UNKNOWN)) {
                    SpecificationConfiguration()
                } else {
                    specificationConfiguration
                }
            }.onErrorResumeNext {
                specificationConfiguration.verificationResult = getVerificationResult(false, null, it)
                return@onErrorResumeNext specificationConfiguration.toSingle()
            }
    }

    private fun loadConfig(): Single<Unit> {
        val configId = selectedCredential.verifiableObject.credential?.getConfigId()
            ?: throw OrganizationException("Cannot find Config Id")
        return configRepo.getConfig(configId).map { response ->
            configType = response.type
            configPayload = response.payload[0]
            parseConfig()
        }
    }

    private fun parseConfig() {
        val type = verifiableObject.type.toString()
        if (configPayload?.configuration != null) {
            val config = configPayload?.configuration?.toString() ?: "{}"
            if (JSONObject(config).has(type)) {
                val configJson = JSONObject(config).getJSONObject(type)
                val ruleSets = configJson.getJSONArray("rule-sets") ?: JSONArray()
                val metrics = configJson.getJSONArray("metrics") ?: JSONArray()

                parseLegacyRules(ruleSets)
                parseLegacyMetrics(metrics)
            }
        } else {
            if (configPayload?.specificationConfigurations != null && configPayload?.specificationConfigurations?.size!! > 0) {
                val config: List<SpecificationConfiguration> = configPayload?.specificationConfigurations!!
                config.forEach {
                    if (it.id.equals(specificationConfiguration?.id)) {
                        val ruleSets = it.rules
                        val metrics = it.metrics

                        parseRules(ruleSets)
                        parseMetrics(metrics)
                    }
                    // Your code here
                }
            }
        }
    }

    // todo: remove when Verifier Config is stable
//    private fun parseHardcodedConfig() {
//        val json = JSONObject(configJson)
//        val payload = json.getJSONArray("payload")
//        for (payloadIndex in 0 until payload.length()) {
//            val configuration = payload
//                .getJSONObject(payloadIndex)
//                .getJSONObject("configuration")
//                .getJSONObject(verifiableObject.type.toString())
//            val ruleSets = configuration.getJSONArray("rule-sets")
//            val metrics = configuration.getJSONArray("metrics")
//
//            parseRules(ruleSets)
//            parseMetrics(metrics)
//        }
//    }

    private fun parseLegacyRules(ruleSets: JSONArray) {
        for (ruleSetIndex in 0 until ruleSets.length()) {
            val rules = ruleSets.getJSONObject(ruleSetIndex).getJSONArray("rules")
            for (ruleIndex in 0 until rules.length()) {
                val rule = rules.getJSONObject(ruleIndex)
                rulesPayloadList.add(parse(rule.toString()))
            }
        }
    }

    private fun parseLegacyMetrics(metricsArray: JSONArray) {
        for (ruleSetIndex in 0 until metricsArray.length()) {
            val metrics = parse<MetricsPayload>(metricsArray.getJSONObject(ruleSetIndex).toString())
            metricsPayloadList.add(metrics)
        }
    }

    private fun parseRules(ruleSets: ArrayList<Rules>) {
        val rulesArray = JSONArray(Gson().toJson(ruleSets))
        for (ruleIndex in 0 until rulesArray.length()) {
            val rule = rulesArray.getJSONObject(ruleIndex)
            rulesPayloadList.add(parse(rule.toString()))
        }
    }

    private fun parseMetrics(metricsList: ArrayList<Metrics>) {
        val metricsArray = JSONArray(Gson().toJson(metricsList))
        for (ruleSetIndex in 0 until metricsArray.length()) {
            val metrics = parse<MetricsPayload>(metricsArray.getJSONObject(ruleSetIndex).toString())
            metricsPayloadList.add(metrics)
        }
    }
    // endregion

    //region metrics
    private fun createAndSubmitMetrics(isNetworkAvailAble: Boolean) {
        disposables.add(
            insertMetrics()
                .flatMap { metricDB.loadAll() }
                .flatMapCompletable {
                    if (isNetworkAvailAble) {
                        submitMetrics(it)
                    } else {
                        Completable.complete()
                    }
                }
                .subscribe({}, rxError("create And Submit Metrics function failed"))
        )
    }

    private fun insertMetrics(): Single<Boolean> {
        return Single.fromCallable {
            val verifierId = selectedCredential.verifiableObject.credential?.id
            val organizationId =
                selectedCredential.verifiableObject.credential?.getOrganizationId()
            val customerId = selectedCredential.verifiableObject.credential?.getCustomerId()
            val spec = verifiableObject.type.name

            val metricsProcessor = MetricsPathProcessor(verifiableObject, metricsPayloadList)
            var issuerId = metricsProcessor.getMetricsFieldByKey("issuerDID")
            var type = metricsProcessor.getMetricsFieldByKey("credentialType")
            var name = metricsProcessor.getMetricsFieldByKey("issuerName")

            if (issuerId.isNullOrEmpty()) issuerId = "Unknown"
            if (type.isNullOrEmpty()) type = "Unknown"
            if (name.isNullOrEmpty()) name = "Unknown"

            Metric(
                status = metricsStatus.value,
                type = type,
                issuerId = issuerId,
                issuerName = name,
                verifierId = verifierId,
                organizationId = organizationId,
                customerId = customerId,
                credentialSpec = spec
            )
        }.flatMap {
            metricDB.insert(it, replace = false, addDuplicate = true)
        }
    }

    private fun submitMetrics(metricTable: Table<Metric>): Completable {
        return if (metricTable.dataList.size >= VerifierConstants.MetricsUploadLimitCount) {
            metricRepo.submitMetrics(metricTable.dataList)
                .map { it.isSuccessful to metricTable.dataList.size }
                .flatMapCompletable {
                    if (it.first ||
                        (!it.first && it.second >= VerifierConstants.MetricsUploadDeleteCount)
                    ) {
                        //Check if Metrics goes over MetricsUploadDeleteCount count in case of failure
                        metricDB.deleteAll()
                    } else {
                        Completable.complete()
                    }
                }
        } else {
            Completable.complete()
        }
    }

    fun VerifyEngine.isTrusted(specificationConfiguration: SpecificationConfiguration): Single<SpecificationConfiguration> {
        return Single.just(specificationConfiguration)
    }

    private fun getVerificationResult(
        valid: Boolean,
        result: VerificationResult?,
        throwable: Throwable? = null
    ): VerificationResult {
        return if (valid) {
            CredentialValid()
        } else {
            result ?: when (throwable) {
                is RulesException -> CredentialRulesInvalid()
                is VerifyError -> CredentialSignatureFailed()
                else -> UnknownVerificationError().also {
                    loge(
                        "UnknownVerificationError",
                        throwable
                    )
                }
            }
        }
    }

//endregion

    override fun onCleared() {
        super.onCleared()
        RxHelper.unsubscribe(disposables)
    }

    fun startCountdown(): Observable<Long> {
        val duration = getAutoDismissDuration().toLong()
        return Observable.interval(1, TimeUnit.SECONDS)
            .take(duration)
            .map { v: Long -> duration - v }
    }

    fun getAutoDismissDuration(): Int {
        return sharedPrefUtils.getInt(SharedPrefUtils.KEY_KIOSK_AUTO_DISMISS_DURATION)
    }

    fun isAutoDismiss(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_KIOSK_AUTO_DISMISS)
    }

    fun isKioskModeEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_KIOSK_MODE)
    }

    fun isSoundEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_SOUND_FEEDBACK)
    }

    fun isHapticEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_HAPTIC_FEEDBACK)
    }

    companion object {
        private val DEFAULT_JSON = "{}".toJsonElement().asJsonObject
        private const val CONFIG_JSON = "verifier_config.json"
        private const val METRICS_INDEX = 0
    }
}