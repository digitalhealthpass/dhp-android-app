package com.merative.watson.healthpass.verifiablecredential.models.credential

import android.os.Parcelable
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.EncryptionKey
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith
import java.text.DateFormat
import java.text.ParseException
import java.util.*

/**
 * If you add custom properties, make sure to use [Exclude] interface on it to tell Gson not to use it
 */
@Parcelize
@TypeParceler<JsonElement, JsonElementParceler>
data class Credential(
    @SerializedName("@context")
    val context: @WriteWith<JsonElementParceler> JsonElement?,//this should be an array, report cases where it is not
    @SerializedName("type")
    val type: List<String>,
    @SerializedName("id")
    val id: String,
    @SerializedName("issuer")
    val issuer: String?,
    @SerializedName("issuanceDate")
    val issuanceDate: String?,
    @SerializedName("expirationDate")
    val expirationDate: String?,
    @SerializedName("credentialSchema")
    val credentialSchema: CredentialSchema?,
    @SerializedName("credentialSubject")
    val credentialSubject: @WriteWith<JsonElementParceler> JsonObject?,
    @SerializedName("proof")
    val proof: Proof?,
    @SerializedName("obfuscation")
    val obfuscation: @WriteWith<JsonElementParceler> JsonElement?
) : Parcelable {

    fun hasObfuscation(): Boolean {
        return !obfuscation.asJsonArrayOrNull().isNullOrEmpty()
    }

    fun hasExpirationDateString(): Boolean = !expirationDate.isNullOrEmpty()

    /**
     * @return true if the date is expired, false otherwise or if there is NO date
     */
    fun isExpired(): Boolean {
        if (expirationDate.isNullOrEmpty()) return false

        return try {
            val date: Date? = expirationDate.toDate(SERVER_DATE_FORMAT, true)
            date != null && date < Date().getCurrentDateInUtcFormat()
        } catch (e: ParseException) {
            Log.e(TAG, "failed to parse expiration date", e)
            false
        }
    }

    fun isValid(): Boolean {
        return !isExpired()
    }

    fun getFormattedExpiryDate(): String {
        if (expirationDate.isNullOrEmpty()) return ""

        return expirationDate.getLocaleFormattedDate(
            dateFormat = SERVER_DATE_FORMAT,
            outputFormat = OUTPUT_DATE_FORMAT
        ).orEmpty()
    }

    fun getFormattedIssuedDate(): String {
        if (issuanceDate.isNullOrEmpty()) return ""

        return issuanceDate.getLocaleFormattedDate(
            dateFormat = SERVER_DATE_FORMAT,
            outputFormat = OUTPUT_DATE_FORMAT
        ).orEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Credential

        if (id != other.id) return false
        if (issuer != other.issuer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (issuer?.hashCode() ?: 0)
        return result
    }

    companion object {
        private const val TAG = "Credential"
        const val OUTPUT_DATE_FORMAT = DateFormat.SHORT

        const val KEY_TYPE = "type"
        const val ID = "id"
    }
}

fun Credential.isProfile() = credentialSubject?.getStringOrNull(Credential.KEY_TYPE) == "profile"
fun Credential.isId() = credentialSubject?.getStringOrNull(Credential.KEY_TYPE) == Credential.ID

fun Credential.getServices(): JsonArray? {
    return credentialSubject?.getAsJsonObject("consentInfo")
        ?.getAsJsonArray("services")
}

fun Credential.getFirstService(): JsonObject? {
    val services = getServices()
    return if (!services.isNullOrEmpty())
        services?.get(0)?.asJsonObject
    else
        null
}

fun Credential.getFirstPurpose(): JsonObject? {
    val purposes = getFirstService()?.asJsonObject?.getAsJsonArray("purposes")
    return if (!purposes.isNullOrEmpty())
        purposes?.get(0)?.asJsonObject
    else
        null
}

fun Credential.getPolicy(): String? {
    return credentialSubject
        ?.getAsJsonObject("consentInfo")
        ?.getStringOrNull("policyUrl")
}

fun Credential.getPoBox(): JsonObject? {
    return credentialSubject
        ?.getAsJsonObject("technical")
        ?.getAsJsonObject("poBox")
}

fun Credential.canUpload(): Boolean {
    return credentialSubject?.getAsJsonObject("technical")
        ?.has("upload") ?: false
}

fun Credential.canDownload(): Boolean {
    return credentialSubject?.getAsJsonObject("technical")
        ?.has("download") ?: false
}

fun Credential.getLinkId(): String {
    //This is in case of NIH
    val poBoxJsonObject = getPoBox()
    var linkId = poBoxJsonObject?.getStringOrNull("linkId")

    //The others should be like this
    if (linkId == null) {
        linkId = credentialSubject
            ?.getAsJsonObject("technical")
            ?.getAsJsonObject(if (canDownload()) "download" else "upload")
            ?.getStringOrNull("linkId").orEmpty()
    }
    return linkId
}

fun Credential.getPassCode(): String {
    //This is in case of NIH
    val poBoxJsonObject = getPoBox()
    var passcode = poBoxJsonObject?.getStringOrNull("passcode")

    //The others should be like this
    if (passcode == null) {
        passcode = credentialSubject
            ?.getAsJsonObject("technical")
            ?.getAsJsonObject(if (canDownload()) "download" else "upload")
            ?.getStringOrNull("passcode").orEmpty()
    }
    return passcode
}

fun Credential.getOrgName(): String? {
    return credentialSubject?.getStringOrNull("organization")
}

fun Credential.getFirstPiiController(): JsonObject? {
    val jsonArray = credentialSubject?.getAsJsonObject("consentInfo")
        ?.getAsJsonArray("piiControllers")
    return if (!jsonArray.isNullOrEmpty())
        jsonArray?.get(0)?.asJsonObject
    else
        null
}

fun Credential.getCustomSymmetricKey(): EncryptionKey? {
    return getPoBox()?.getAsJsonObject("symmetricKey")?.let {
        val algorithm = it.get("algorithm").asString
        val iv = it.get("iv").asString
        val value = it.get("value").asString
        EncryptionKey(algorithm, iv, value)
    }
}

fun Credential.getDefaultSymmetricKey(): EncryptionKey? {
    return credentialSubject?.getAsJsonObject("technical")?.getAsJsonObject("symmetricKey")?.let {
        val algorithm = it.get("algorithm").asString
        val iv = it.get("iv").asString
        val value = it.get("value").asString
        EncryptionKey(algorithm, iv, value)
    }
}

val Credential.credentialSubjectType: String?
    get() {
        return credentialSubject?.getStringOrNull("type")
    }

fun Credential.isVaccinationCard(): Boolean {
    return this.credentialSubjectType.equals("Vaccination Card", true).or(false)
}

fun Credential.getCustomerId(): String? {
    return credentialSubject?.getStringOrNull("customerId")
}

fun Credential.getOrganizationId(): String? {
    return credentialSubject?.getStringOrNull("organizationId")
}

fun Credential.toVerifiableObject() = VerifiableObject(this.stringfy())

fun Credential.getConfigId(): String? {
    if (credentialSubject?.getStringOrNull("configId")?.split(':')?.size!! > 0) {
        val idString = credentialSubject.getStringOrNull("configId")?.split(':')?.get(0)
        return idString
    } else return credentialSubject.getStringOrNull("configId")
}

fun Credential.getConfigVersion(): String? {
    if (credentialSubject?.getStringOrNull("configId")?.split(':')?.size!! > 1) {
        val idVersion = credentialSubject.getStringOrNull("configId")?.split(':')?.get(1)
        return idVersion
    }
    return null
}

fun Credential.isNotValid() = id.isNullOrEmpty()
        || getCustomerId().isNullOrEmpty()
        || getOrganizationId().isNullOrEmpty()