package com.merative.healthpass.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun Uri.getMimeType(context: Context): String? {
    //Check uri format to avoid null
    return if (scheme == ContentResolver.SCHEME_CONTENT) {
        //If scheme is a content
        val mime = MimeTypeMap.getSingleton()
        mime.getExtensionFromMimeType(context.contentResolver.getType(this))
    } else {
        //If scheme is a File
        //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
        MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(path)).toString())
    }
}
