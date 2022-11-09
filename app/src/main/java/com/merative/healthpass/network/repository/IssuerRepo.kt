package com.merative.healthpass.network.repository

import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.models.*
import com.merative.healthpass.models.api.ApiDataException
import com.merative.healthpass.models.api.IssuerResponse
import com.merative.healthpass.models.api.ShcPublicKey
import com.merative.healthpass.network.serviceinterface.IssuerService
import com.merative.healthpass.utils.pref.DccIssuerDB
import com.merative.healthpass.utils.pref.IssuerDB
import com.merative.healthpass.utils.pref.ShcIssuerDB
import com.merative.healthpass.utils.pref.Table
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.known.UnKnownTypeException
import com.merative.watson.healthpass.verificationengine.models.issuer.IssuerKey
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class IssuerRepo @Inject constructor(
    private val issuerService: IssuerService,
    private val issuerDB: IssuerDB,
    private val shcIssuerDB: ShcIssuerDB,
    private val dccIssuerDB: DccIssuerDB
) {

    fun loadAndSaveSignatureKeys(): Single<Unit> {
        return loadAndSaveMetadata(VCType.IDHP)
            .flatMap { loadAndSaveMetadata(VCType.SHC) }
            .flatMap { loadAndSaveMetadata(VCType.DCC) }
            .flatMap { Single.just(Unit) }
    }

    private fun loadAndSaveMetadata(
        vcType: VCType,
        verifiableObject: VerifiableObject? = null,
        bookmark: String? = null
    ): Single<IssuerMetaData> {
        return if (verifiableObject == null) {
            loadKeysByType(vcType, bookmark)
        } else {
            loadSingleKeyByType(verifiableObject, bookmark)
        }.map { response ->
            if (!response.isSuccessfulAndHasBody()) {
                throw OfflineModeException(response)
            }
            response.body()!!
        }.flatMap { parsePublicKeyResponse(vcType, it) }
            .flatMap { savePublicKeys(it) }
            .flatMap { loadNextPages(it, vcType, verifiableObject) }
    }

    private fun loadKeysByType(type: VCType, bookmark: String?) =
        when (type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> issuerService.getIdhpGhpVcIssuers()
            VCType.SHC -> issuerService.getShcIssuers(PAGE_SIZE, bookmark)
            VCType.DCC -> issuerService.getDccIssuers(PAGE_SIZE, bookmark)
            else -> throw UnKnownTypeException()
        }

    private fun loadSingleKeyByType(
        verifiableObject: VerifiableObject,
        bookmark: String?
    ): Single<Response<IssuerResponse>> {
        return when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> issuerService.getIdhpGhpVcIssuersByIssuerId(
                verifiableObject.credential!!.issuer!!
            )
            VCType.SHC -> issuerService.getShcIssuers(PAGE_SIZE, bookmark)
            VCType.DCC -> issuerService.getDccIssuersByKeyId(
                verifiableObject.cose!!.keyId,
                PAGE_SIZE
            )
            else -> throw UnKnownTypeException()
        }
    }

    private fun parsePublicKeyResponse(
        type: VCType,
        response: IssuerResponse
    ): Single<IssuerMetaData> {
        return Single.fromCallable {
            when (type) {
                VCType.IDHP, VCType.GHP, VCType.VC -> {
                    val keys = response.payload
                    val metaData = VcIssuerMetaData()
                    if (keys?.isJsonArray == true) {
                        metaData.issuerList = parse(keys.toString())
                    } else if (keys?.isJsonObject == true) {
                        metaData.issuerList = listOf(parse(keys.toString()))
                    }
                    metaData
                }
                VCType.SHC -> {
                    val keysPayload = parsePkPayload(response)
                    val count = getRecordCount(response).orValue(0)
                    val bookmark = if (count > 0) getBookmark(response) else null
                    val keys = parse<List<ShcPublicKey>>(keysPayload)
                    ShcIssuerMetaData(bookmark = bookmark, publicKeyList = keys)
                }
                VCType.DCC -> {
                    val keys = parsePkPayload(response)
                    val count = getRecordCount(response).orValue(0)
                    val bookmark = if (count > 0) getBookmark(response) else null
                    val issuerKeys = parse<List<IssuerKey>>(keys)
                    DccIssuerMetaData(bookmark = bookmark, issuerKeysList = issuerKeys)
                }
                else -> throw UnKnownTypeException()
            }
        }
    }

    private fun parsePkPayload(response: IssuerResponse) =
        response.payload
            ?.asJsonObject
            ?.getAsJsonArray("payload")
            .toString()

    private fun getBookmark(response: IssuerResponse) =
        response.payload
            ?.asJsonObject
            ?.getAsJsonPrimitive("bookmark")
            ?.asString

    private fun getRecordCount(response: IssuerResponse) =
        response.payload
            ?.asJsonObject
            ?.getAsJsonPrimitive("record_count")
            ?.asInt

    private fun savePublicKeys(issuerMetaData: IssuerMetaData) = when (issuerMetaData) {
        is VcIssuerMetaData -> issuerDB.insertAll(issuerMetaData.issuerList!!, true)
        is ShcIssuerMetaData -> shcIssuerDB.insertAll(issuerMetaData.publicKeyList!!, true)
        is DccIssuerMetaData -> dccIssuerDB.insertAll(issuerMetaData.issuerKeysList!!, true)
        else -> Single.error(IllegalStateException("publicKeys == null"))
    }.flatMap { Single.just(issuerMetaData) }

    private fun loadNextPages(
        it: IssuerMetaData,
        vcType: VCType,
        verifiableObject: VerifiableObject?
    ) = if (it.bookmark != null && vcType != VCType.DCC) {
        loadAndSaveMetadata(vcType, verifiableObject, it.bookmark)
    } else {
        Single.just(it)
    }

    fun getIssuerFromDb(
        verifiableObject: VerifiableObject,
        downloadIfNotExist: Boolean = true
    ): Single<IssuerMetaData> {
        return Single.just(verifiableObject.type)
            .flatMap { type -> loadIssuerByTypeFromDb(type, verifiableObject) }
            .flatMap { keys ->
                return@flatMap if (downloadIfNotExist) {
                    checkKeysAvailability(verifiableObject, keys)
                } else {
                    Single.just(keys)
                }
            }
    }

    private fun loadIssuerByTypeFromDb(type: VCType, verifiableObject: VerifiableObject) =
        when (type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> {
                verifiableObject.credential?.issuer?.let { issuerID ->
                    issuerDB.loadAll()
                        .map {
                            VcIssuerMetaData(issuer = it.dataList.filter { issuerID == it.id }
                                .firstOrNull())
                        }
                } ?: Single.error(ApiDataException("IDHP, GHP, VC: ID is null"))
            }
            VCType.SHC -> {
                verifiableObject.jws?.payload?.iss?.let { iss ->
                    var name: String? = null
                    shcIssuerDB.loadAll()
                        .map { name = getShcKeyName(it, iss); it }
                        .map { getShcKeys(it, iss) }
                        .map { ShcIssuerMetaData(jwkList = it, issuerName = name) }
                } ?: Single.error(ApiDataException("SHS: Issuer URL is null"))
            }
            VCType.DCC -> {
                verifiableObject.cose?.keyId?.let { kid ->
                    dccIssuerDB.loadAll()
                        .map { it.dataList.filter { it.kid == kid } }
                        .map { DccIssuerMetaData(issuerKeysList = it) }
                } ?: Single.error(ApiDataException("DCC: KID is null"))
            }
            else -> throw UnKnownTypeException()
        }

    private fun getShcKeys(it: Table<ShcPublicKey>, iss: String) = it.dataList
        .filter { it.url?.contains(iss) == true }
        .mapNotNull { it.keys }
        .flatten()

    private fun getShcKeyName(it: Table<ShcPublicKey>, iss: String) = it.dataList
        .filter { it.url?.contains(iss) == true }
        .mapNotNull { it.name }
        .firstOrNull()

    private fun checkKeysAvailability(
        verifiableObject: VerifiableObject,
        keys: IssuerMetaData
    ) = checkKeysAvailabilityByType(verifiableObject, keys)
        .flatMap { available -> getKeysOrRequestFromApi(available, keys, verifiableObject) }

    private fun checkKeysAvailabilityByType(
        verifiableObject: VerifiableObject,
        keys: IssuerMetaData
    ) = Single.fromCallable {
        return@fromCallable when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> (keys as VcIssuerMetaData).issuer != null
            VCType.SHC -> (keys as ShcIssuerMetaData).jwkList?.isNotEmpty().orValue(false)
            VCType.DCC -> (keys as DccIssuerMetaData).issuerKeysList?.isNotEmpty().orValue(false)
            else -> throw UnKnownTypeException()
        }
    }

    private fun getKeysOrRequestFromApi(
        keysAvailable: Boolean,
        keys: IssuerMetaData,
        verifiableObject: VerifiableObject
    ) = if (keysAvailable) Single.just(keys) else repeatKeysUpload(verifiableObject, keys.bookmark)

    private fun repeatKeysUpload(
        verifiableObject: VerifiableObject,
        bookmark: String?
    ): Single<IssuerMetaData> {
        return loadSingleKeyByType(verifiableObject, bookmark)
            .map { response ->
                if (!response.isSuccessfulAndHasBody()) {
                    throw OfflineModeException(response)
                }
                response.body()!!
            }.flatMap { parsePublicKeyResponse(verifiableObject.type, it) }
            .flatMap { savePublicKeys(it) }
            .flatMap { loadNextPages(it, verifiableObject.type, verifiableObject) }
            .flatMap { loadIssuerByTypeFromDb(verifiableObject.type, verifiableObject) }
    }

    fun resetIssuerInformation(): Completable =
        issuerDB.deleteAll()
            .andThen(shcIssuerDB.deleteAll())
            .andThen(dccIssuerDB.deleteAll())

    companion object {
        const val PAGE_SIZE = 200
    }
}