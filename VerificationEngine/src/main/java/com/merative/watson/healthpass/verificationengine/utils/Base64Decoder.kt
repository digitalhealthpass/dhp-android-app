package com.merative.watson.healthpass.verificationengine.utils

import android.util.Base64

class Base64Decoder {

    companion object {

        fun decode(message: String?): ByteArray? {
            if (message == null) return null

            var updatedMessage =
                message.replace("-", "+")
                    .replace("_", "/")

            when {
                updatedMessage.length % 4 == 2 -> updatedMessage += "=="
                updatedMessage.length % 4 == 3 -> updatedMessage += "="
            }

            return try {
                Base64.decode(updatedMessage, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}