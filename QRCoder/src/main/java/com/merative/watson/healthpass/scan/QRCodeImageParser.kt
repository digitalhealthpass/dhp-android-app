package com.merative.watson.healthpass.scan

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import com.merative.watson.healthpass.interfaces.Consumer
import com.merative.watson.healthpass.utils.DecoderResult
import com.merative.watson.healthpass.utils.QRCodeDecoder
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.ref.WeakReference

class QRCodeImageParser<T>(fragment: Fragment) {
    private val weakFragment = WeakReference(fragment)

    var onSuccessListener: Consumer<DecoderResult<T>>? = null
    var onErrorListener: Consumer<Exception>? = null

    fun start() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        weakFragment.get()?.startActivityForResult(
            Intent.createChooser(intent, "Select image with:"),
            CODE_IMAGE
        )
    }

    fun startWithMultipleSelection() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        weakFragment.get()?.startActivityForResult(
            Intent.createChooser(intent, "Select image with:"),
            CODE_IMAGE
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val exception = Exception("File Invalid")
            val imagesEncodedList: MutableList<String> = ArrayList<String>().toMutableList()
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            if (data.clipData != null) {
                val clipData = data.clipData
                val arrayUri = ArrayList<Uri>()

                for (i in 0 until clipData!!.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    arrayUri.add(uri)

                    // Get the cursor
                    val cursor: Cursor? =
                        weakFragment.get()?.requireActivity()?.getContentResolver()
                            ?.query(uri, filePathColumn, null, null, null)

                    cursor?.moveToFirst()
                    val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn.get(0))
                    val imageEncoded = columnIndex?.let { cursor.getString(it) }

                    if (imageEncoded != null)
                        imagesEncodedList += imageEncoded

                    if (data?.data == null || weakFragment.get() == null) {
                        cursor?.close()
                        processUri(uri)
                    } else if (data.data == null || weakFragment.get() == null) {
                        onErrorListener?.onNext(NullPointerException("The received Data or Fragment reference is null"))
                        Log.e(TAG, exception.toString())
                        return
                    } else {
                        val uri: Uri = data.data!!
                        processUri(uri)
                    }
                }
            } else {
                if (data?.data == null || weakFragment.get() == null) {
                    onErrorListener?.onNext(NullPointerException("The received Data or Fragment reference is null"))
                    Log.e(
                        TAG,
                        "The uri is null, probably the user cancelled the image selection process using the back button.",
                        exception
                    )
                    return
                }
                val uri: Uri = data.data!!
                processUri(uri)
            }
        }
    }

    fun processUri(uri: Uri, item: T? = null) {
        val exception = Exception("File Invalid")
        try {
            val inputStream: InputStream =
                weakFragment.get()!!.requireActivity().contentResolver.openInputStream(uri)!!
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                onErrorListener?.onNext(exception)
                Log.e(TAG, "uri is not a bitmap, $uri")
                return
            }
            QRCodeDecoder(onSuccessListener, onErrorListener).decodeBitmap(bitmap, item)
        } catch (e: FileNotFoundException) {
            onErrorListener?.onNext(e)
            Log.e(TAG, "can not open file: $uri", e)
        }
    }

    fun onDestroy() {
        weakFragment.clear()
        onSuccessListener = null
        onErrorListener = null
    }

    companion object {
        const val CODE_IMAGE = 1305
        private const val TAG = "QRCodeImageParser"
    }
}