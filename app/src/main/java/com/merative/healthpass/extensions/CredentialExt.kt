package com.merative.healthpass.extensions

import android.content.Context
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.models.sharedPref.CredentialDownloaded
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

fun Credential.toCredentialDownloaded(): CredentialDownloaded {
    return CredentialDownloaded(id, type)
}

/**
 * switch the Credential to json file
 * @param context
 * @param optionalName sending empty will fall back to [Credential.id] of the credential
 */
fun Credential.toFile(context: Context, optionalName: String): File {
    // Define the File Path and its Name
    val parent = File("${context.filesDir}/${AppConstants.FOLDER_TEMP}")
    if (!parent.exists()) parent.mkdirs()

    val file = File(
        "${context.filesDir}/${AppConstants.FOLDER_TEMP}",
        "${optionalName.ifEmpty { id }}.w3c"
    )

    val fileWriter = FileWriter(file)
    val bufferedWriter = BufferedWriter(fileWriter)
    bufferedWriter.write(stringfy())
    bufferedWriter.close()

    return file
}