package com.merative.healthpass.extensions

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun File.contentToString(): String {
    return bufferedReader().use { it.readText() }
}

fun InputStream.contentToString(): String {
    val bytes = ByteArray(available())
    read(bytes, 0, bytes.size)
    return String(bytes)
}

fun InputStream.toFile(context: Context, name: String): File? {
    var file: File? = null
    try {
        val parent = File("${context.filesDir}/importing")
        if (!parent.exists()) parent.mkdirs()

        //w3c
        file = File("${context.filesDir}/importing", name)

        val out: OutputStream = FileOutputStream(file)
        var size = 0
        val buffer = ByteArray(1024)
        while (read(buffer).also { size = it } != -1) {
            out.write(buffer, 0, size)
        }
        out.close()
    } catch (e: Exception) {
        loge("failed to convert to a file", e)
        file?.delete()
    }
    return file
}