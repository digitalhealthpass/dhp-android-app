package com.merative.watson.healthpass.verifiablecredential.models

/**
 * local data class to hold the encryption data
 */
data class EncryptionKey(val algorithm: String, val iv: String, val keyValue: String)