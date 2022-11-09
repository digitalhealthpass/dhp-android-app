package com.merative.healthpass.models.credential

import android.os.Parcelable
import android.util.Base64
import com.merative.healthpass.extensions.loge
import kotlinx.parcelize.Parcelize
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

@Parcelize
data class AsymmetricKey(
    val publicKey: String,
    val privateKey: String,
    var tag: String,
    /**this time stamp is in UTC*/
    val timeStamp: Long
) : Parcelable {

    fun getPublicKeyWithHeaders(): String {
        return addHeaders(publicKey)
    }

    fun getPrivateKeyWithHeaders(): String {
        return addHeaders(privateKey)
    }

    private fun addHeaders(key: String): String {
        val headline = "-----BEGIN PUBLIC KEY-----\n"
        val footline = "-----END PUBLIC KEY-----\n"
        return headline + key + footline
    }

    //region equals and hashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AsymmetricKey

        if (publicKey != other.publicKey) return false
        if (privateKey != other.privateKey) return false
        if (timeStamp != other.timeStamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + privateKey.hashCode()
        result = 31 * result + timeStamp.hashCode()
        return result
    }
    //endregion

    companion object {
        /**
         * @return Pair of public and private keys
         */
        fun createKeyTest(): AsymmetricKey? {
            try {
                val kpg = KeyPairGenerator.getInstance("RSA")
                kpg.initialize(2048)
                val keyPair: KeyPair = kpg.genKeyPair()
                val pri: ByteArray = keyPair.private.encoded
                val pub: ByteArray = keyPair.public.encoded
//            val privateKey = String(pri)
//            val publicKey = String(pub)

                val privateKey: String = Base64.encodeToString(pri, Base64.NO_WRAP)
                val publicKey: String = Base64.encodeToString(pub, Base64.NO_WRAP)

//            logd("Private Key: $privateKey")
//            logd("Public Key: $publicKey")

                return AsymmetricKey(publicKey, privateKey, "Untitled", Date().time)
            } catch (ex: NoSuchAlgorithmException) {
                loge("failed to create public private keys", ex)
                return null
            }
        }
    }
}
