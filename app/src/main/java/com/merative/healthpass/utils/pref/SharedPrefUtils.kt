package com.merative.healthpass.utils.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.extensions.toArrayList
import com.merative.healthpass.models.User
import com.merative.healthpass.models.pin.PinCode
import com.merative.healthpass.models.pin.hasPinCode
import com.merative.healthpass.models.sharedPref.AutoReset
import com.merative.healthpass.models.sharedPref.Package
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.util.*

class SharedPrefUtils(context: Context) {
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    private val PASS_KEY = context.getString(R.string.pass_file_key)
    private val sharedPrefs = EncryptedSharedPreferences.create(
        PASS_KEY,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    //region basic helpers
    fun getString(key: String): String? {
        return sharedPrefs.getString(key, null)
    }

    fun putString(key: String, data: String, commit: Boolean = false) {
        sharedPrefs.edit(commit) {
            putString(key, data)
        }
    }

    fun getBoolean(key: String): Boolean {
        if (key == KEY_SOUND_FEEDBACK || key == KEY_HAPTIC_FEEDBACK) {
            return sharedPrefs.getBoolean(key, true)
        } else
            return sharedPrefs.getBoolean(key, false)
    }

    fun putBoolean(key: String, data: Boolean, commit: Boolean = false) {
        sharedPrefs.edit(commit) {
            putBoolean(key, data)
        }
    }

    fun putInt(key: String, data: Int) {
        sharedPrefs.edit {
            putInt(key, data)
        }
    }

    fun getInt(key: String): Int {
        return sharedPrefs.getInt(key, 0)
    }

    fun contains(key: String): Boolean {
        return sharedPrefs.contains(key)
    }

    /**
     * remove all prefs
     */
    fun clearAllPrefs(commit: Boolean) {
        sharedPrefs.edit(commit) {
            clear()
        }
    }

    fun clearPrefs(key: String) {
        sharedPrefs.edit {
            remove(key)
        }
    }

    /**
     * get the size in bytes for shared preference for a String key
     */
    fun getPrefSize(key: String): Long {
        val data: String = getString(key).orEmpty()
        val byteArray = data.toByteArray(charset("UTF-8"))
        return byteArray.size.toLong()
    }
    //endregion

    /**
     * Listen to the changes in SharedPreference, keep in mind it is the future changes, there is No initial values
     */
    fun listenToKeysChanges(): Flow<Pair<SharedPreferences, String>> {
        return channelFlow {
            //1) register
            //2) emit
            val subscription =
                SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    channel.offer(sharedPreferences to key)
                }
            sharedPrefs.registerOnSharedPreferenceChangeListener(subscription)

            // 3) Don't close the stream of data, keep it open until the consumer
            // stops listening or the API calls onCompleted or onError.
            // When that happens, cancel the subscription to the 3P library
            awaitClose {
                sharedPrefs.unregisterOnSharedPreferenceChangeListener(subscription)
            }
        }
    }

    //region user helpers
    fun saveUser(user: User, commit: Boolean = true) {
        putString(
            KEY_USER,
            user.stringfy(),
            commit
        )
    }

    fun getUser(): User? {
        val userJson = getString(KEY_USER)
        return if (userJson.isNotNullOrEmpty()) {
            parse(userJson!!)
        } else {
            null
        }
    }

    fun removeUser(commit: Boolean = false) {
        sharedPrefs.edit(commit) { remove(KEY_USER) }
    }
    //endregion

    //region credentials
    /**
     * this will import data into database and will override the current data with the incoming one,
     * if it is the same object
     */
    fun importData(context: Context): Completable {
        return Single.fromCallable {
            val filesDir = requireNotNull(context.filesDir.absoluteFile)
            val restoredDir = File(filesDir.absolutePath + "/restored")
            val fileName = restoredDir.listFiles()[0].name
            val jsonString =
                File(restoredDir.absolutePath, fileName).bufferedReader().use { it.readText() }

            //old code was saving only a list
            parseDBJson(context, jsonString)
        }
            .flatMapCompletable { jsonObject ->
                Completable.mergeArray(
                    //TODO need to be injected
                    PackageDB(this)
                        .importDB(jsonObject.getAsJsonObject(PackageDB.DB_NAME)),
                    ContactDB(this)
                        .importDB(jsonObject.getAsJsonObject(ContactDB.DB_NAME)),
                    AsymmetricKeyDB(this)
                        .importDB(jsonObject.getAsJsonObject(AsymmetricKeyDB.DB_NAME)),
                )
            }
            .andThen {
                val contactDB = ContactDB(this)
                val asymmetricKeyDB = AsymmetricKeyDB(this)
                ContactKeyDBMigration(contactDB, asymmetricKeyDB).start()
                it.onComplete()
            }
            .doOnComplete {
                val filesDir = requireNotNull(context.filesDir.absoluteFile)
                val restoredDir = File(filesDir.absolutePath + "/restored")
                //delete json file after mapping to sharedPreferences
                val deleted = restoredDir.deleteRecursively()
            }
    }

    private fun parseDBJson(context: Context, jsonString: String): JsonObject {
        //this is for backward compatibility, 1st version was saving a list of object immediately
        return when (jsonString.toJsonElement()) {
            is JsonArray -> {
                //parse it as backup object
                val credList = parse<List<Package>>(jsonString)
                val table =
                    Table(PackageDB.VERSION, credList.toArrayList())

                val jsonObject = JsonObject()
                jsonObject.add(PackageDB.DB_NAME, table.toJsonElement().asJsonObject)

                jsonObject
            }
            else -> {
                jsonString.toJsonElement().asJsonObject
            }
        }
    }
    //endregion

    //region pin code
    fun getPin(): PinCode {
        val pinCodeJson =
            getString(KEY_PIN).orValue(PinCode.EMPTY.stringfy())
        return parse(pinCodeJson)
    }

    fun hasPin(): Boolean {
        val pinCodeJson =
            getString(KEY_PIN).orValue(PinCode.EMPTY.stringfy())
        val pinCode = parse<PinCode>(pinCodeJson)
        return pinCode.hasPinCode()
    }

    fun hasPinSkipped(): Boolean {
        val pinCodeJson =
            getString(KEY_PIN).orValue(PinCode.EMPTY.stringfy())
        val pinCode = parse<PinCode>(pinCodeJson)
        return pinCode == PinCode.SKIPPED
    }
    //endregion

    //region auto reset
    fun getAutoReset(): AutoReset {
        val autoResetJson = getString(KEY_AUTO_RESET)
        if (autoResetJson.isNullOrEmpty()) {
            setAutoReset(AutoReset.EMPTY)
            return AutoReset.EMPTY
        }
        return parse(autoResetJson)
    }

    fun setAutoReset(autoReset: AutoReset, isAutoReset: Boolean = false) {
        if (isAutoReset) {
            autoReset.lastAutoResetDate = Calendar.getInstance()
        }
        putString(KEY_AUTO_RESET, autoReset.stringfy(), true)
    }
    //endregion

    /**
     * this should be used for development only
     */
    fun resetFirstTimeExperience() {
        sharedPrefs.edit(true) {
            putBoolean(KEY_TERMS_ACCEPTED, false)
            putBoolean(KEY_PRIVACY_ACCEPTED, false)
            putBoolean(KEY_FILE_ACCESS, false)
            putBoolean(KEY_TUTORIAL_SHOWN, false)
            putString(KEY_ENV_REGION, null)
            putString(KEY_CAMERA_PERMISSION, null)
        }
    }

    companion object {
        const val KEY_USER = "user"
        const val KEY_TERMS_ACCEPTED = "termsAccepted"
        const val KEY_PRIVACY_ACCEPTED = "privacyAccepted"
        const val KEY_FILE_ACCESS = "isGranted"
        const val KEY_CAMERA_PERMISSION = "isCameraGranted"
        const val KEY_TUTORIAL_SHOWN = "tutorial"

        const val KEY_PIN = "pin_code_model"
        const val KEY_ENV_REGION = "app_env_region"

        const val KEY_AUTO_RESET = "auto_reset"

        const val KEY_KIOSK_MODE = "kiosk_mode"
        const val KEY_KIOSK_AUTO_DISMISS = "kiosk_auto_dismiss"
        const val KEY_KIOSK_AUTO_DISMISS_DURATION = "kiosk_auto_dismiss_duration"
        const val KEY_KIOSK_FRONT_CAMERA = "kiosk_auto_front_camera"

        const val KEY_SOUND_FEEDBACK = "sound_feedback"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    }
}