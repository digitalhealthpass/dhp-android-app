package com.merative.healthpass.utils

import android.content.Context
import android.os.Build
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.logi
import com.merative.healthpass.extensions.rxError
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class Utils {

    /**
     * This is used to clear the Temp folder which is used while Sharing credentials
     */
    fun clearSharedFolder(context: Context) {
        Completable.fromCallable {
            val parent = File("${context.filesDir}/${AppConstants.FOLDER_TEMP}")
            val size = if (parent.exists())
                parent.listFiles()?.size
            else
                0

            if (parent.exists()) {
                parent.listFiles()?.forEach {
                    it.delete()
                }
                val deleted = parent.delete()
                logi("share folder contained $size file(s) and was deleted= $deleted")
            }
        }.subscribeOn(Schedulers.io())
            .subscribe({}, rxError("failed to clear the shared directory"))
    }

    object StringUtils {

        fun isAllValid(vararg args: String?): Boolean {
            //checking all strings passed and if a single string is not valid returning false.
            args.forEach {
                if (isNotValid(it))
                    return false
            }
            return true
        }

        fun isValid(string: String?): Boolean {
            return string != null && string.isNotEmpty() && string.isNotBlank() && !string.equals("null")
        }

        fun isNotValid(string: String?): Boolean {
            return isValid(string).not()
        }

    }
    object WalletDeviceUtils {
        fun getCountryIsoCode(): String? {
            return try {
                Class.forName("android.os.SystemProperties").let { cls ->
                    cls.getMethod("get", String::class.java).let { method ->
                        method.invoke(cls, "ro.csc.countryiso_code") as String
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
        fun getModelName(): String?{
            return Build.MODEL
        }
    }
}