package com.merative.healthpass.utils.schema

import android.os.Parcelable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.*
import com.merative.healthpass.utils.schema.Field.Companion.KEY_LOCATION
import com.merative.healthpass.utils.schema.Field.Companion.TYPE_ARRAY
import com.merative.healthpass.utils.schema.Field.Companion.TYPE_BOOLEAN
import com.merative.healthpass.utils.schema.Field.Companion.TYPE_STRING
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.parcelize.WriteWith
import java.util.*
import kotlin.collections.HashMap

/**
 * since this object has Any for the [value] Serializable is needed, specially in view like [RegistrationDetailsFragment]
 */
@Parcelize
class Field(
    var path: String,
    var value: @RawValue Any,
    var obfuscatedValueObject: @WriteWith<JsonElementParceler> JsonObject?,
) : Parcelable, java.io.Serializable {

    var dataType: String = "not-initialized"
    var format: String = ""
    var description: String = ""
    var pattern: String = ""
    var visible: Boolean = true
    var foundInSchema = false
    var enum: List<*>? = null
    var displayValueJson: JsonObject? = null
    var displayValueArray: JsonArray? = null
    var typeValueMap: HashMap<String, String> = HashMap()
    var displayType: String = ""
    var order: Int? = null
    var required = false
    var minLength: Int? = null
    var maxLength: Int? = null
    var sectionIndex: Int? = null
    var sectionTitle: JsonObject? = null
    var valueMapper: String? = null
    var recordStatus: String? = null
    var valueAdditionalData: String? = null
    var isObfuscated: Boolean = false

    /**
     * This will try to check the [value] if it is a string and a color then it will return it,
     *  otherwise will try to look for color in the [enum],
     * if it exists, then it will return the next value in the [enum] list if exists.
     *
     * Otherwise, it will return default credential local color
     */
    val colorFromEnums: String
        get() {
            return if (value is String) {
                val stringValue = value.toString()
                if (stringValue.isColor()) {
                    stringValue
                } else {
                    val colorFromEnum = colorMaps[stringValue]
                    if (colorFromEnum == null) {
                        loge("$stringValue wasn't found in the colors map neither hex decimal, will revert back to the default local color")
                    }

                    colorFromEnum.orValue(AppConstants.COLOR_CREDENTIALS)
                }
            } else {
                loge("color $value is not a valid value to be used")
                AppConstants.COLOR_CREDENTIALS
            }
        }

    /**this is used for views only*/
    var editable = false

    /**
     * return Pair of Path (last word if multiple exists) and value as string
     */
    fun getUsableValue(locale: Locale): Pair<String, String> {
        //label to use
        val langCode = locale.language
        val validPath = displayValueJson?.getStringOrNull(langCode)
            .orValue(
                displayValueJson?.getStringOrNull(DEFAULT_LANG)
                    .orValue(path.substringAfterLast("."))
            )            //fall back to path
            .splitCamelCase().capitalize().replace("\\s+".toRegex(), " ")

        //value to use
        //get the obfuscated value if exits, otherwise use the value itself
        val usedValue =
            obfuscatedValueObject?.getStringOrNull(KEY_OBF_VAL).orValue(value.toString())

        return validPath to usedValue
    }

    fun adjustDisplayValueIfNeeded() {
        if (displayValueJson == null && displayValueArray != null) {
            val usedValue =
                obfuscatedValueObject?.getStringOrNull(KEY_OBF_VAL).orValue(value.toString())

            displayValueJson = displayValueArray?.find {
                it.asJsonObject.getStringOrNull(KEY_TYPE) == usedValue
            }?.asJsonObject?.getAsJsonObject(KEY_DISPLAY_VALUE)
        }
    }

    override fun toString(): String {
        return "Field{" +
                "path='" + path + '\'' +
                ", type='" + dataType + '\'' +
                ", visible=" + visible +
                ", value=" + value +
                '}'
    }

    companion object {
        //region constants
        const val KEY_PROPERTIES = "properties"
        const val KEY_ITEMS = "items"
        const val KEY_TYPE = "type"
        const val KEY_FORMAT = "format"
        const val KEY_DESCRIPTION = "description"
        const val KEY_VISIBLE = "visible"
        const val KEY_ENUM = "enum"
        const val KEY_PATTERN = "pattern"
        const val KEY_LOCATION = "location"
        const val KEY_MIN_LENGTH = "minLength"
        const val KEY_MAX_LENGTH = "maxLength"
        const val KEY_DISPLAY_TYPE = "displayType"
        const val KEY_DISPLAY_VALUE = "displayValue"
        const val KEY_DISPLAY_VALUE_ARRAY = "displayValueArray"
        const val KEY_ORDER = "order"
        const val KEY_REQUIRED = "required"

        //obfuscation keys
        const val KEY_OBF_VAL = "val"

        //This can be both ID or a Public Key
        const val ID = "id"

        const val KEY = "key"

        //field names
        const val FIELD_NAME_DISPLAY = "display"
        const val FIELD_NAME_PUBLIC_KEY = "publicKey"
        const val RACE = "race"

        const val REGISTRATION_CODE = "registrationCode"
        const val ORGANIZATION_CODE = "organization"

        const val FORMAT_DATE_TIME = "date-time"
        const val FIELD_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        const val TYPE_ARRAY = "array"
        const val TYPE_STRING = "string"
        const val TYPE_BOOLEAN = "boolean"

        //possible values in json response
        const val RACE_DECLINE_TO_STATE = "Decline to state"

        const val DEFAULT_LANG = "en"
        //endregion

        fun create(path: String, value: String): Field {
            return Field(path, value, null).apply {
                visible = true
                dataType = "string"
            }
        }

        private val colorMaps = HashMap<String, String>()
            .apply {
                put("red", "#FF0000")
                put("green", "#00ff00")
                put("limegreen", "#32CD32")
                put("orange", "#FFA500")
                put("yellow", "#FFFF00")
                put("grey", "#808080")
                put("gray", "#808080")
                put("blue", "#007bff")
            }
    }
}

//region extensions
fun Field.getFirstPathPart(defaultIfNone: String = ""): String {
    val index = path.indexOf(".")

    return if (index > 0)
        path.substringBefore(".")
    else
        defaultIfNone
}

fun Field.getLastPathPart(): String {
    val lastIndex = path.lastIndexOf(".")

    return if (lastIndex > 0)
        path.substring(lastIndex + 1)
    else
        path
}

fun Field.getSectionHeader(locale: Locale): String {
    return sectionTitle
        ?.getStringOrNull(locale.language)
        .orValue(sectionTitle?.getStringOrNull(Field.DEFAULT_LANG).orValue(""))
        .splitCamelCase()
        .capitalize()
}

fun Field.isStringAndHasEnums(): Boolean = enum != null && dataType == TYPE_STRING
fun Field.isArray(): Boolean = dataType == TYPE_ARRAY
fun Field.isLocation(): Boolean = path.equals(KEY_LOCATION, true)
fun Field.hasAList() = isArray() || isStringAndHasEnums() || isLocation()

fun Field.isString(): Boolean = dataType == TYPE_STRING
fun Field.isBoolean(): Boolean = dataType == TYPE_BOOLEAN
//endregion