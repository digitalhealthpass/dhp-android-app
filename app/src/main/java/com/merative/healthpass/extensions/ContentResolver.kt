package com.merative.healthpass.extensions

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

fun ContentResolver.getContentName(uri: Uri): String? {
    val cursor: Cursor? = query(uri, null, null, null, null)
    cursor?.moveToFirst()
    val nameIndex: Int =
        cursor?.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME).orValue(-1)
    return if (nameIndex >= 0) {
        cursor?.getString(nameIndex)
    } else {
        null
    }
}
