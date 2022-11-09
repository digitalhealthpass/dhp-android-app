package com.merative.healthpass.network.repository

import android.content.Context
import com.google.gson.JsonElement
import com.merative.healthpass.common.VerifierConstants
import com.merative.healthpass.extensions.loadAssetsFileAsString
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration
import com.merative.healthpass.utils.schema.DisplayFieldProcessor
import com.merative.healthpass.utils.schema.Field
import com.merative.watson.healthpass.verifiablecredential.extensions.isNullOrEmpty
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

class DisplayFieldsRepo @Inject constructor(
    private val context: Context
) {
    fun getLegacyDisplayFields(
        verifiableObject: VerifiableObject,
        configuration: JsonElement?
    ): Single<List<Pair<String, String>>> {
        return Single.fromCallable {
            val fieldList: List<Field> =
                DisplayFieldProcessor(loadFieldLegacyJson(verifiableObject.type, configuration))
                    .getDisplayFields(verifiableObject)

            return@fromCallable fieldList.map { it.getUsableValue(Locale.getDefault()) }
        }
    }

    fun getDisplayFields(
        verifiableObject: VerifiableObject,
        configuration: SpecificationConfiguration?
    ): Single<List<Pair<String, String>>> {
        return Single.fromCallable {
            val fieldList =
                DisplayFieldProcessor(loadFieldJson(configuration))
                    .getDisplayFields(verifiableObject)

            return@fromCallable fieldList.map { it.getUsableValue(Locale.getDefault()) }
        }
    }

    //API Version
    private fun loadFieldLegacyJson(type: VCType, jsonConfig: JsonElement?): String {
        val displayArray = jsonConfig?.asJsonObject?.getAsJsonObject(type.name)
            ?.getAsJsonArray("display")
        return if (displayArray.isNullOrEmpty()) {
            "[]"
        } else {
            displayArray?.get(0)?.asJsonObject
                ?.getAsJsonArray("fields")
                ?.orValue("[]")
                .toString()
        }
    }

    private fun loadFieldJson(configuration: SpecificationConfiguration?): String {
        val displayArray = configuration?.display
        return if (displayArray.isNullOrEmpty()) {
            "[]"
        } else {
            displayArray[0].fields.toJsonElement()
                .orValue("[]")
                .toString()
        }
    }

    @Deprecated("This was for hardcoded config. Use the one with 2 parameters.")
    private fun loadFieldJson(type: VCType) =
        context.loadAssetsFileAsString(VerifierConstants.DISPLAY_SET_PATH_LOCATION)
            .toJsonElement()
            .asJsonObject
            .getAsJsonArray(type.name)
            .toString()
}