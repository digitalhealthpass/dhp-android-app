package com.merative.healthpass.models.sharedPref

import android.os.Parcelable
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.models.api.registration.uploadCredential.CredentialProcessed
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.utils.schema.Field
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.EncryptionKey
import com.merative.watson.healthpass.verifiablecredential.models.credential.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactPackage(
    val profilePackage: Package?,
    val idPackage: Package?,
    val asymmetricKey: AsymmetricKey?,
    /**this is saved for local info only, try to avoid using it*/
    var contactType: String
) : Parcelable {
    val uploadedCredentialList: ArrayList<CredentialProcessed> = ArrayList()
    val downloadedCredentialList: ArrayList<CredentialDownloaded> = ArrayList()

    /**
     * @return public key from the [Credential] object if exists, otherwise it will fall back to the [AsymmetricKey.publicKey]
     */
    fun getIdCredSubjectKey(): String? {
        var key = idPackage?.verifiableObject?.credential
            ?.credentialSubject?.getStringOrNull(Field.KEY)
        if (key == null) {
            key = idPackage?.verifiableObject?.credential
                ?.credentialSubject?.getStringOrNull(Field.ID)
        }
        if (key == null) {
            key = asymmetricKey?.publicKey
        }
        return key
    }

    fun getSymmetricKey(): EncryptionKey? {
        return if (isDefaultPackage()) {
            profilePackage?.verifiableObject?.credential?.getDefaultSymmetricKey()
        } else {
            profilePackage?.verifiableObject?.credential?.getCustomSymmetricKey()
        }
    }

    fun getOrgId(): String {
        var orgID =
            profilePackage?.verifiableObject?.credential?.credentialSubject?.getStringOrNull("orgId")

        //For backward compatibility with old NIH credentials
        if (orgID == null) {
            orgID = profilePackage?.schema?.authorName?.toLowerCase().orValue(contactType)
        }
        return orgID
    }

    fun canDownload(): Boolean {
        return profilePackage?.verifiableObject?.credential?.canDownload().orValue(false)
    }

    fun canUpload(): Boolean {
        return profilePackage?.verifiableObject?.credential?.canUpload().orValue(false)
                || !isDefaultPackage()
    }

    fun isDefaultPackage(): Boolean {
        return !getOrgId().contains(TYPE_NIH, true)
    }

    fun getDocumentURL(): String {
        return if (isDefaultPackage()) {
            profilePackage?.verifiableObject?.credential?.credentialSubject?.getAsJsonObject("technical")
                ?.getAsJsonObject("upload")
                ?.getStringOrNull("url").orEmpty()
        } else {
            profilePackage?.verifiableObject?.credential?.getPoBox()?.getStringOrNull("url")
                .orEmpty()
        }
    }

    fun getContactName(): String {
        return if (isDefaultPackage()) {
            profilePackage?.verifiableObject?.credential?.credentialSubject?.getStringOrNull("name")
                .orEmpty()
        } else {
            profilePackage?.verifiableObject?.credential
                ?.getFirstPiiController()?.getStringOrNull("piiController").orEmpty()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactPackage

        if (profilePackage?.verifiableObject?.credential?.id != other.profilePackage?.verifiableObject?.credential?.id) return false
        if (profilePackage?.verifiableObject?.credential?.proof?.signatureValue != other.profilePackage?.verifiableObject?.credential?.proof?.signatureValue) return false
        if (idPackage?.verifiableObject?.credential?.id != other.idPackage?.verifiableObject?.credential?.id) return false
        if (idPackage?.verifiableObject?.credential?.proof?.signatureValue != other.idPackage?.verifiableObject?.credential?.proof?.signatureValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = profilePackage?.verifiableObject?.credential?.id?.hashCode() ?: 0
        result = 31 * result +
                (profilePackage?.verifiableObject?.credential?.proof?.signatureValue?.hashCode() ?: 0)
        result = 31 * result + (idPackage?.verifiableObject?.credential?.id?.hashCode() ?: 0)
        result = 31 * result +
                (idPackage?.verifiableObject?.credential?.proof?.signatureValue?.hashCode() ?: 0)
        return result
    }

    companion object {
        //custom contacts
        const val TYPE_NIH = "nih"
    }
}
