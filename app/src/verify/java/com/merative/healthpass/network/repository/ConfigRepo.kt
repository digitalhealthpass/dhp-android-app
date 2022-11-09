package com.merative.healthpass.network.repository

import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.models.api.ConfigResponse
import com.merative.healthpass.network.serviceinterface.ConfigService
import com.merative.healthpass.utils.pref.ConfigDB
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

class ConfigRepo @Inject constructor(
    private val configService: ConfigService,
    private val configDB: ConfigDB
) {

    fun getConfig(id: String): Single<ConfigResponse> {
        return getConfigFromDB(id)
            .flatMap { resList ->
                if (resList.isEmpty()) {
                    return@flatMap configService.getConfig(id).map {
                        if (!it.isSuccessfulAndHasBody()) {
                            throw HttpException(it)
                        }
                        it.body()!!
                    }
                } else {
                    Single.just(resList[0])
                }
            }
    }

    private fun getConfigFromDB(id: String): Single<List<ConfigResponse>> {
        return configDB.loadAll()
            .map { response ->
                response.dataList.filter {
                    it.payload?.id == id
                }
            }
    }

    fun updateConfigCache(id: String, version: String): Single<Boolean> {
        return getConfigFromDB(id)
            .flatMap { config ->
                if (configNeedsUpdate(config)) {
                    loadAndInsertConfig(id, version)
                        .flatMap {
                            if (it == true) {
                                configDB.deleteList(config).andThen(Single.just(true))
                            } else {
                                Single.just(it)
                            }
                        }
                } else {
                    Single.just(false)
                }
            }
    }

    fun configNeedsUpdate(filteredList: List<ConfigResponse>): Boolean {
        if (filteredList.isEmpty()) {
            return true
        }
        val config = filteredList[0]
        val payload = config.payload
        val downloadDate = config.saveTime
        val refreshPeriod =
            (Date().time - downloadDate.time) / 1000
        if (payload?.refresh == null) {
            return false
        }
        if (refreshPeriod > payload.refresh!!) {
            return true
        }
        return false
    }

    fun loadAndInsertConfig(id: String, version: String): Single<Boolean> {
        // for now we do not check "offline" field
        return configService
            .getConfig(id, version)
            .flatMap {
                val body = it.body()
                if (body != null) {
                    body.saveTime = Date()
                    configDB.insert(
                        model = body, replace = true, addDuplicate = false
                    )
                } else {
                    Single.just(false)
                }
            }
    }

}