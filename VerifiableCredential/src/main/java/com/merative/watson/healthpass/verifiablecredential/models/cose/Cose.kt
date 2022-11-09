package com.merative.watson.healthpass.verifiablecredential.models.cose

import android.util.Base64
import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.extensions.getOrNull
import com.merative.watson.healthpass.verifiablecredential.models.cwt.CWT
import com.merative.watson.healthpass.verifiablecredential.utils.Base45Decoder
import com.upokecenter.cbor.CBORObject
import java.util.zip.InflaterInputStream

/**
 * keep in mind if you use the other constructor with only String, it can return a Null object
 */
class Cose(HC1Body: String) {

    init {
        invoke(HC1Body)
    }

    var type: CoseType? = null
    var protectedHeader: CoseHeader? = null
    var unprotectedHeader: CoseHeader? = null
    lateinit var payload: CBORObject

    /** This is used for verification */
    lateinit var payloadBytes: ByteArray
    lateinit var signature: CBORObject

    val keyId: String
        get() {
            val header = unprotectedHeader?.keyId?.GetByteString()
                ?: protectedHeader?.keyId?.GetByteString()
            // TODO("somehow remove replaces. maybe another encoding")
            return Base64
                .encodeToString(header, Base64.DEFAULT)
                .replace("\n", "")
        }

    val signatureStruct: ByteArray?
        get() = if (type == CoseType.SIGNATURE_1) getValidationData() else null

    val hCertJson: String?
        get() = payload.getOrNull(CWT.INDEX_HCERT)?.getOrNull(1)?.ToJSONString()

    /**
     * this is used only for [signatureStruct] field
     */
    private fun getValidationData() =
        CBORObject.NewArray().apply {
            Add(this@Cose.type!!.value)
            Add(protectedHeader?.rawHeader?.GetByteString())
            Add(ByteArray(0))
            Add(payloadBytes)
        }.EncodeToBytes()

    private fun invoke(HC1Body: String) {

        val base45Decoded: ByteArray = Base45Decoder().decode(HC1Body)
        val decompressed: ByteArray? = decompressBase45DecodedData(base45Decoded)

        if (decompressed != null) {

            val cbor = CBORObject.DecodeFromBytes(decompressed)
            Log.d(TAG, "messageObject: $cbor")

            val coseType =
                CoseType.values().firstOrNull { it.tag == cbor.mostOuterTag.toString() }
            val payloadCbor = cbor.getOrNull(2)
            val protectedHeaderCbor = cbor.getOrNull(0)
            val unprotectedHeaderCbor = cbor.getOrNull(1)
            val signature = cbor.getOrNull(3)

            if (payloadCbor == null || signature == null) {
                Log.e(TAG, "Cose - Payload or signature = null")
                throw IllegalStateException("Cose - Payload or signature = null")
            }

            val payloadDecoded = CBORObject.DecodeFromBytes(payloadCbor.GetByteString())
            val protectedHeader = decodeCoseHeader(protectedHeaderCbor)
            val unprotectedHeader = decodeCoseHeader(unprotectedHeaderCbor)

            Log.d(TAG, "payload : $payloadDecoded")
            Log.d(TAG, "protectedHeader : $protectedHeader")
            Log.d(TAG, "unprotectedHeader : $unprotectedHeader")


            init(
                coseType,
                protectedHeader,
                unprotectedHeader,
                payloadDecoded,
                payloadCbor.GetByteString(),
                signature
            )

            Log.d(
                TAG,
                "Cose: $this, keyId: ${this.keyId}, signatureStruct: ${this.signatureStruct}"
            )
        }
    }

    private fun decodeCoseHeader(header: CBORObject?): CoseHeader? {
        if (header == null) return null

        val headerBytes: ByteArray? = try {
            header.GetByteString()
        } catch (e: IllegalStateException) {
            null
        }

        val keyIdKey = CoseHeaderKeys.KID.asCBOR()
        val algKey = CoseHeaderKeys.ALGORITHM.asCBOR()

        val keyId = if (headerBytes == null) {
            header.get(keyIdKey)
        } else {
            CBORObject.DecodeFromBytes(headerBytes).get(keyIdKey)
        }

        val algCbor = headerBytes?.let { CBORObject.DecodeFromBytes(it).get(algKey) }
        val algorithm = Algorithm.values().firstOrNull { it.asCBOR() == algCbor }

        return CoseHeader(header, keyId, algorithm)
    }

    private fun decompressBase45DecodedData(base45Decoded: ByteArray?): ByteArray? {

        if (base45Decoded == null) return null

        // ZLIB magic headers
        return if (base45Decoded.size >= 2 && base45Decoded[0] == 0x78.toByte() && (
                    base45Decoded[1] == 0x01.toByte() || // Level 1
                            base45Decoded[1] == 0x5E.toByte() || // Level 2 - 5
                            base45Decoded[1] == 0x9C.toByte() || // Level 6
                            base45Decoded[1] == 0xDA.toByte()
                    )
        ) {
            InflaterInputStream(base45Decoded.inputStream()).readBytes()
        } else base45Decoded
    }

    private fun init(
        type: CoseType?,
        protectedHeader: CoseHeader?,
        unprotectedHeader: CoseHeader?,
        payload: CBORObject,
        /** This is used for verification */
        payloadBytes: ByteArray,
        signature: CBORObject
    ) {
        this.type = type
        this.protectedHeader = protectedHeader
        this.unprotectedHeader = unprotectedHeader
        this.payload = payload
        this.payloadBytes = payloadBytes
        this.signature = signature
    }

    companion object {

        val TAG = Cose::javaClass.name
    }

    init {
        invoke(HC1Body)
    }
}

val Cose.cwt: CWT
    get() = CWT(payload)
