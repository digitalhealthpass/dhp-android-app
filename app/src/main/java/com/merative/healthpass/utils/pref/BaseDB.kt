package com.merative.healthpass.utils.pref

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.merative.healthpass.extensions.*
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.watson.healthpass.verifiablecredential.extensions.format
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class BaseDB<Model>(protected val sharedPrefUtils: SharedPrefUtils) {
    private val empty: Table<Model> = Table.getEmptyTable(version)
    abstract val dbName: String
    abstract val version: Int

    fun loadAll(): Single<Table<Model>> {
        return Single.fromCallable {
            try {
                val json = sharedPrefUtils.getString(dbName).orValue(empty.stringfy())
                parse(json)
            } catch (ex: Exception) {
                loge("failed to parse the loaded list, so will delete all", ex)
                sharedPrefUtils.putString(dbName, empty.stringfy())
                empty
            }
        }.asyncToUiSingle()
    }

    protected fun parse(jsonString: String): Table<Model> {
        val classT: Class<*> = (javaClass
            .genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        val type = TypeToken.getParameterized(Table::class.java, classT).type
        return GsonHelper.gson.fromJson<Table<Model>>(jsonString, type)
    }

    fun insert(
        model: Model,
        replace: Boolean,
        addDuplicate: Boolean = false
    ): Single<Boolean> {
        return loadAll()
            .flatMap { loadedValue ->
                Single.fromCallable {
                    val exist = loadedValue.dataList.contains(model)

                    if (!exist || replace) {
                        loadedValue.dataList.addOrReplace(model, true)
                        sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                        true
                    } else if (addDuplicate) {
                        loadedValue.dataList.add(model)
                        sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                        true
                    } else {
                        false
                    }
                }
            }
    }

    fun insertAll(
        modelList: List<Model>,
        replace: Boolean,
        addDuplicate: Boolean = false
    ): Single<Boolean> {
        return loadAll()
            .flatMap { loadedValue ->
                Single.fromCallable {
                    var dataExists = false

                    loadedValue.dataList.forEach {
                        if (modelList.contains(it)) {
                            dataExists = true
                            return@forEach
                        }
                    }
                    if (!dataExists || replace) {
                        loadedValue.dataList.addOrReplaceAll(modelList, true)
                        sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                        true
                    } else if (addDuplicate) {
                        loadedValue.dataList.addAll(modelList)
                        sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                        true
                    } else {
                        false
                    }
                }.asyncToUiSingle()
            }
    }

    fun delete(model: Model?): Completable {
        return if (model == null) {
            return Completable.complete()
        } else {
            loadAll()
                .flatMapCompletable { loadedValue ->
                    Completable.fromAction {
                        loadedValue.dataList.remove(model)

                        sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                    }
                }
        }
    }

    fun deleteList(models: List<Model>): Completable {
        return loadAll()
            .flatMapCompletable { loadedValue ->
                Completable.fromAction {
                    loadedValue.dataList.removeAll(models)

                    sharedPrefUtils.putString(dbName, loadedValue.stringfy())
                }
            }
    }

    fun deleteAll(): Completable {
        return Completable.fromCallable {
            sharedPrefUtils.clearPrefs(dbName)
        }
    }

    /**
     * this will overwrite the information in current DB with imported one
     */
    fun importDB(dbObject: JsonObject?): Completable {
        if (dbObject == null)
            return Completable.complete()

        return loadAll()
            .flatMapCompletable { migrateDB(parse(dbObject.toString()), it) }
    }

    protected open fun migrateDB(dbObject: Table<Model>, loadedValue: Table<Model>): Completable {
        return Completable.fromCallable {
            dbObject.dataList.filterNotNull().forEach {
                loadedValue.dataList.remove(it)
            }

            loadedValue.dataList.addAll(dbObject.dataList)

            sharedPrefUtils.putString(dbName, loadedValue.stringfy())
        }
    }

    /**
     * @return A [Triple] that contains DB name, db JsonObject, true if database is empty or false otherwise
     */
    fun getTableJson(): Single<Triple<String, JsonObject, Boolean>> {
        return loadAll()
            .map {
                Triple(dbName, it.toJsonElement().asJsonObject, it.dataList.isEmpty())
            }
    }

    /**
     * get the DB size in bytes
     */
    fun getSize(): Long = sharedPrefUtils.getPrefSize(dbName)

    companion object {
        val ARCHIVE_NAME = "Holder_Archive_${Date().format()}"
    }
}

data class Table<Model>(
    /** Use this to track the changes in DB, in case you need to do migrations*/
    @SerializedName("version")
    val version: Int,
    @SerializedName("dataList", alternate = ["credentialAndSchemaList"])
    val dataList: ArrayList<Model>
) {
    companion object {
        fun <Model> getEmptyTable(version: Int) = Table<Model>(version, ArrayList())
    }
}
