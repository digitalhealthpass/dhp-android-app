package com.merative.healthpass.models

import com.merative.healthpass.models.api.ShcPublicKey
import com.merative.watson.healthpass.verificationengine.models.issuer.Issuer
import com.merative.watson.healthpass.verificationengine.models.issuer.IssuerKey
import com.merative.watson.healthpass.verificationengine.models.issuer.JWK

abstract class IssuerMetaData(
    open var bookmark: String? = null,
    open var issuerName: String? = null
)

data class VcIssuerMetaData(
    var issuer: Issuer? = null,
    var issuerList: List<Issuer>? = null,
) : IssuerMetaData()

data class ShcIssuerMetaData(
    var jwkList: List<JWK>? = null,
    var publicKeyList: List<ShcPublicKey>? = null,
    override var bookmark: String? = null,
    override var issuerName: String? = null
) : IssuerMetaData()

data class DccIssuerMetaData(
    var issuerKeysList: List<IssuerKey>? = null,
    override var bookmark: String? = null,
    override var issuerName: String? = null
) : IssuerMetaData()