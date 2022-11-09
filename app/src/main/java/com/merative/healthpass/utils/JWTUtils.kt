package com.merative.healthpass.utils

import android.util.Base64
import android.util.Log
import com.merative.healthpass.extensions.loge
import java.io.UnsupportedEncodingException

class JWTUtils {
    companion object {

        fun decoded(jwtEncoded: String): String {
            try {
                val split = jwtEncoded.split("\\.".toRegex())
                Log.d("JWT_ENCODED", "Header: " + getJson(split[0]))
                Log.d("JWT_ENCODED", "Body: " + getJson(split[1]))
                return getJson(split[1])
            } catch (e: UnsupportedEncodingException) {
                loge("failed to decode", e)
                return ""
            }
        }

        private fun getJson(strEncoded: String): String {
            val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, Charsets.UTF_8)
        }
    }
}