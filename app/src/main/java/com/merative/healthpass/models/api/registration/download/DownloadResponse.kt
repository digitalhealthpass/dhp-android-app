package com.merative.healthpass.models.api.registration.download

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.merative.healthpass.extensions.decrypt
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.sharedPref.Package
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.EncryptionKey
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadResponse(
    val type: String,
    val payload: Payload
) : BaseResponse(), Parcelable

@Parcelize
data class Payload(
    val id: String,
    val attachments: List<Attachment>
) : Parcelable {
    /**
     * @param encryptionKey this should be the [ContactPackage#profilePackage#getSymmetricKey()] key
     */
    fun toPackage(encryptionKey: EncryptionKey?): List<Package>? {
        if (encryptionKey == null)
            return null
        return attachments.map {
            Package(
                VerifiableObject(it.toCredentialJsonString(encryptionKey)!!),
                null,
                null
            )
        }
    }
}

@Parcelize
data class Attachment(
    val id: String,
    val link: String,
    val type: String,
    val name: String,
    val content: String,
    val accessed: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("expires_at")
    val expiresAt: String,
) : Parcelable {
    /**
     * @param encryptionKey this should be the [ContactPackage#profilePackage#getSymmetricKey()] key
     */
    fun toCredential(encryptionKey: EncryptionKey?): Credential? {
        if (encryptionKey == null)
            return null
        return parse(toCredentialJsonString(encryptionKey)!!)
    }

    /**
     * @param encryptionKey this should be the [ContactPackage#profilePackage#getSymmetricKey()] key
     * @return the Credential in json string format
     */
    fun toCredentialJsonString(encryptionKey: EncryptionKey?): String? {
        if (encryptionKey == null)
            return null
        return content.decrypt(encryptionKey)
    }
}

