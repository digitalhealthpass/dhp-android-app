package com.merative.healthpass.models.sharedPref

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.models.api.metadata.IssuerMetaData
import com.merative.healthpass.models.api.schema.SchemaPayload
import com.merative.healthpass.utils.schema.SchemaProcessor
import com.merative.watson.healthpass.verifiablecredential.interfaces.Exclude
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Package(
    @SerializedName("credential")
    val verifiableObject: VerifiableObject,
    @SerializedName("schema")
    var schema: SchemaPayload?,
    @SerializedName("metaDataPayLoad")
    val issuerMetaData: IssuerMetaData?,
) : Parcelable {

    /**this is used for UI only*/
    @IgnoredOnParcel
    var isSelected: Boolean = false

    @IgnoredOnParcel
    @Exclude
    var hexColor: String? = null

    @IgnoredOnParcel
    @Exclude
    var issuerName: String? = null

    fun processHexColor() {
        when (verifiableObject.type) {
            VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC -> {
                SchemaProcessor().processSchemaAndSubject(this, true)
            }
            VCType.SHC -> {
                hexColor = AppConstants.COLOR_SHC_CREDENTIAL
            }
            VCType.DCC -> {
                hexColor = AppConstants.COLOR_DCC_CREDENTIAL
            }
            else -> {
                hexColor = AppConstants.COLOR_CREDENTIALS
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Package

        if (verifiableObject != other.verifiableObject) return false

        return true
    }

    override fun hashCode(): Int {
        return verifiableObject.hashCode()
    }
}

fun Package.getIssuerName(): String {
    return when (verifiableObject.type) {
        VCType.SHC -> {
            issuerName ?: ""
        }
        VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC, VCType.CONTACT -> {
            issuerMetaData?.metadata?.name.orEmpty()
        }
        VCType.DCC -> {
            verifiableObject.cose?.cwt?.iss?.let { iss ->
                Locale.getAvailableLocales().asList()
                    .firstOrNull { it.country == iss }?.displayCountry
            } ?: ""
        }
        else -> ""
    }
}

fun Package.getSchemaName(): String {
    return when (verifiableObject.type) {
        VCType.SHC -> {
            verifiableObject.jws?.payload?.vc?.type
                ?.map { type -> type.split("#").let { it[it.size - 1] }.capitalize() }
                .toString()
                .let { it.substring(1, it.length - 1) }
        }
        VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC, VCType.CONTACT -> {
            schema?.name.orEmpty()
        }
        VCType.DCC -> {
            verifiableObject.cose?.cwt?.certificate?.getType()?.displayValue ?: ""
        }
        else -> ""
    }
}