package com.merative.healthpass.utils

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.ByteArrayInputStream


class ZipUtils {
    fun createZipFile(filePath: String, inputStream: ByteArrayInputStream, password: String) {
        val zipParameters = ZipParameters()
        zipParameters.fileNameInZip = "credentials-schemas.json"
        zipParameters.isEncryptFiles = true
        zipParameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
        ZipFile(filePath, password.toCharArray()).addStream(inputStream, zipParameters)
    }
}