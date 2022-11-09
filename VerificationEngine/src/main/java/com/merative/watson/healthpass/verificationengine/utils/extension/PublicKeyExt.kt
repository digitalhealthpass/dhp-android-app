package com.merative.watson.healthpass.verificationengine.utils.extension

import android.util.Base64
import android.util.Log
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.VerifyEngine.Companion.TAG
import com.merative.watson.healthpass.verificationengine.models.issuer.JWK
import com.merative.watson.healthpass.verificationengine.utils.Base64Decoder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

fun stringPkToCertificate(pubKey: String): X509Certificate {
    val decoded = Base64Decoder.decode(pubKey)
    val inputStream: InputStream = ByteArrayInputStream(decoded)
    return CertificateFactory.getInstance("X.509")
        .generateCertificate(inputStream) as X509Certificate
}

fun VerifyEngine.convertJwkToSecKey(publicKeyJwk: JWK): ECPublicKey? {
    if (publicKeyJwk.crv != VerifyEngine.VALID_CRV) {
        Log.e(TAG, "Unknown curve algorithm for public key ${publicKeyJwk.crv}")
        return null
    }
    val bytex = Base64.decode(publicKeyJwk.x, Base64.URL_SAFE)
    val pkx = BigInteger(1, bytex)

    val bytey = Base64.decode(publicKeyJwk.y, Base64.URL_SAFE)
    val pky = BigInteger(1, bytey)

    val pubPoint = ECPoint(pkx, pky)
    val parameters = AlgorithmParameters.getInstance("EC")
    parameters.init(ECGenParameterSpec(VerifyEngine.EC_NAME))
    val ecParameters = parameters.getParameterSpec(ECParameterSpec::class.java)
    val pubSpec = ECPublicKeySpec(pubPoint, ecParameters)

    val kf = KeyFactory.getInstance("EC")
    val genPk = kf.generatePublic(pubSpec) as ECPublicKey
    Log.d(TAG, "FOUND PUBLIC KEY ${Base64.encodeToString(genPk.encoded, Base64.DEFAULT)}")
    Log.d(TAG, "FOUND PUBLIC KEY $genPk")
    return genPk
}