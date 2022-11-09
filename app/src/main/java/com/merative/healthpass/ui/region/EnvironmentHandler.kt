package com.merative.healthpass.ui.region

import com.google.gson.GsonBuilder
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.logd
import com.merative.healthpass.extensions.loge
import com.merative.healthpass.models.region.Env
import com.merative.healthpass.models.serializer.EnvDeserializer
import com.merative.healthpass.models.serializer.EnvSerializer
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class EnvironmentHandler @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils
) {
    val envList = ArrayList<Env>()

    val currentEnv: Env
        get() {
            val json = sharedPrefUtils.getString(SharedPrefUtils.KEY_ENV_REGION).orEmpty()

            return if (json.isEmpty()) {
                getUsaDependingOnBuild()
            } else {
                try {
                    parse(json, gson)
                } catch (ex: Exception) {
                    loge("failed to parse Env and will return the USA version", ex, true)
                    getUsaDependingOnBuild()
                }
            }
        }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Env::class.java, EnvSerializer())
        .registerTypeAdapter(Env::class.java, EnvDeserializer())
        .disableHtmlEscaping()
        .create()

    init {
        loadOldRegionIfExist()
        addEnvironments()
        addProdEnvironments()
        sortEnvironments()
    }

    private fun sortEnvironments() {
        envList.sortBy { it.name }
    }

    private fun addEnvironments() {
        if (BuildConfig.DEBUG) {
            envList.apply {
                add(Env.AUSTRALIA_SANDBOX)
                add(Env.USA_SANDBOX_1)
                add(Env.USA_SANDBOX_2)
                add(Env.USA_DEV_1)
                add(Env.USA_DEV_2)
                add(Env.USA_DEV_3)
                add(Env.USA_QA)
                add(Env.USA_STAGE_2)
                add(Env.EMEA_QA)
                add(Env.EMEA_QA_2)
            }
        }
    }

    private fun addProdEnvironments() {
        envList.apply {
            add(Env.USA)
            add(Env.ASIA)
            add(Env.EMEA)
            add(Env.CANADA)
        }
    }

    fun shouldDisableFeatureForRegion(): Boolean {
        return !(currentEnv.name.toLowerCase(Locale.getDefault()).contains("usa"))
    }

    /**
     * This will save the new environment and remove the saved user
     */
    fun putEnvironment(env: Env) {
        sharedPrefUtils.putString(SharedPrefUtils.KEY_ENV_REGION, env.stringfy(gson))
        //this is needed since we are saving user access token
        sharedPrefUtils.removeUser(true)
    }

    private fun getUsaDependingOnBuild(): Env {
        return when {
            BuildConfig.DEBUG -> Env.USA_DEV_1
            else -> Env.USA
        }
    }

    fun isEnvRegionSet(): Boolean {
        return sharedPrefUtils.getString(SharedPrefUtils.KEY_ENV_REGION).isNotNullOrEmpty()
    }

    /**
     * this is needed to load the old Region object from v103 then migrate
     */
    private fun loadOldRegionIfExist() {
        val oldRegionJson = sharedPrefUtils.getString("app_region")

        if (oldRegionJson.isNotNullOrEmpty()) {
            val code = oldRegionJson?.toJsonElement().asJsonObjectOrNull()
                ?.getStringOrNull("code")
                .orEmpty()

            val envToReturn = if (code.equals("emea", true)) {
                Env.EMEA
            } else {
                Env.USA
            }

            logd("loaded old region: $oldRegionJson and will clear it")
            sharedPrefUtils.clearPrefs("app_region")
            sharedPrefUtils.putString(SharedPrefUtils.KEY_ENV_REGION, envToReturn.stringfy(gson))
            logd("saved env: $envToReturn")
        }
    }
}