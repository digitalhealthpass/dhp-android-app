package com.merative.healthpass.extensions

import com.google.crypto.tink.subtle.Base64
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.watson.healthpass.verifiablecredential.models.EncryptionKey
import okio.ByteString.Companion.decodeBase64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

//private const val MODE = "Blowfish/CBC/PKCS5Padding"
//private const val ALGORITHM = "Blowfish"

private const val SHA256_RSA_PSS_MODE = "SHA256withRSA/PSS"
private const val RSA = "RSA"
private const val AES_MODE = "AES/CBC/PKCS5Padding"
private const val ALGORITHM = "AES"

fun String.encrypt(encryptionKey: EncryptionKey): String {
    return encrypt(encryptionKey.algorithm, encryptionKey.iv, encryptionKey.keyValue)
}

/**
 * encrypt the string using given values
 * @param iv this will be decoded to base64 and then used
 * @param keyValue this will be decoded to base64 and then used
 */
@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun String.encrypt(algorithm: String, iv: String, keyValue: String): String {
    val cipher: Cipher = Cipher.getInstance(AES_MODE)
    val secretKeySpec = SecretKeySpec(keyValue.decodeBase64()?.toByteArray(), algorithm)
    cipher.init(
        Cipher.ENCRYPT_MODE,
        secretKeySpec,
        IvParameterSpec(iv.decodeBase64()?.toByteArray())
    )
    val values: ByteArray = cipher.doFinal(toByteArray())
    return Base64.encodeToString(values, Base64.DEFAULT)
}

fun String.sign(asymmetricKey: AsymmetricKey?): String? {
    if (asymmetricKey == null) {
        loge("failed to sign because AsymmetricKey is null")
        return null
    }

    val privateKeyEncoded = Base64.decode(asymmetricKey.privateKey, Base64.DEFAULT)
    val keySpec = PKCS8EncodedKeySpec(privateKeyEncoded)
    val keyFactory = KeyFactory.getInstance(RSA)
    val privateKey = keyFactory.generatePrivate(keySpec)

    val data = toByteArray(charset("UTF8"))

    val signature = Signature.getInstance(SHA256_RSA_PSS_MODE)
    signature.initSign(privateKey)
    signature.update(data)
    val signatureBytes = signature.sign()

    return Base64.encodeToString(signatureBytes, Base64.DEFAULT or Base64.NO_WRAP).trim()
}

fun String.decrypt(encryptionKey: EncryptionKey): String {
    return decrypt(encryptionKey.algorithm, encryptionKey.iv, encryptionKey.keyValue)
}

/**
 * decrypt the string using given values
 * @param iv this will be decoded to base64 and then used
 * @param keyValue this will be decoded to base64 and then used
 */
@Throws(
    NoSuchPaddingException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun String.decrypt(algorithm: String, iv: String, keyValue: String): String {
    val values: ByteArray = Base64.decode(this, Base64.DEFAULT)
    val secretKeySpec = SecretKeySpec(keyValue.decodeBase64()?.toByteArray(), algorithm)
    val cipher: Cipher = Cipher.getInstance(AES_MODE)
    cipher.init(
        Cipher.DECRYPT_MODE,
        secretKeySpec,
        IvParameterSpec(iv.decodeBase64()?.toByteArray())
    )
    return String(cipher.doFinal(values))
}