package com.merative.healthpass.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.extensions.*
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.ZipUtils
import com.merative.healthpass.utils.asyncToUiCompletable
import com.merative.healthpass.utils.pref.BaseDB
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class SettingsFragment : BaseSettingsFragment() {

    private var importPass: String? = null

    private val deleteAllDisposable = SerialDisposable()
    private val importDisposable = SerialDisposable()
    private val disposables = CompositeDisposable()

    private var dialogVisible = false

    override fun setupTopUI(root: View) {

        binding.settingsTopLayout.buttonPin.setOnClickListener {
            navigateSafe(R.id.action_settings_to_pinSettings)
        }

        binding.settingsTopLayout.createBackupButton.setOnClickListener {
            showCreateBackupAlertDialog()
        }

        binding.settingsTopLayout.importBackupButton.setOnClickListener {
            onLocalRestoreRequested()
        }
    }

    private fun showCreateBackupAlertDialog() {
        if (!dialogVisible) {
            dialogVisible = true
            activity?.showInputDialog(
                getString(R.string.title_backup_file),
                getString(R.string.label_placeholder_password),
                getString(R.string.title_create),
                { _, password ->

                    if (password.isEmpty()) {
                        view?.snackShort(getString(R.string.password_empty_message))
                    } else {
                        disposables.add(
                            viewModel.getBackupJson()
                                .subscribe({ backupJsonString ->
                                    if (backupJsonString.isNullOrEmpty()) {
                                        view?.snackShort(
                                            getString(R.string.payload_empty_message)
                                        )
                                    } else {
                                        val extraString = if (BuildConfig.DEBUG)
                                            "_${viewModel.environmentHandler.currentEnv.title}"
                                        else ""

                                        val fileName = "${BaseDB.ARCHIVE_NAME}$extraString.zip"
                                        val filePath = requireContext().filesDir.path + "/$fileName"
                                        val zipUtils = ZipUtils()
                                        zipUtils.createZipFile(
                                            filePath,
                                            backupJsonString.byteInputStream(),
                                            password
                                        )
                                        shareFile(
                                            filePath,
                                            REQUEST_CODE_CREATE_CREDENTIAL,
                                            FILE_TYPE_ZIP
                                        )
                                    }
                                }, rxError("failed to load credential and schema"))
                        )
                    }
                },
                getString(R.string.button_title_cancel), { },
                { dialogVisible = false },
                true
            )
        }
    }

    private fun onLocalRestoreRequested() {
        if (!dialogVisible) {
            dialogVisible = true
            activity?.showInputDialog(
                getString(R.string.title_import_file),
                getString(R.string.label_placeholder_password),
                getString(R.string.title_import),
                { _, text ->
                    importPass = text
                    showFileImport(REQUEST_CODE_IMPORT)
                },
                getString(R.string.button_title_cancel), { },
                { dialogVisible = false },
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_IMPORT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    parseImportFiles(data)
                }
            }
            REQUEST_CODE_CREATE_CREDENTIAL -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //in case you want to take an action
                }
            }
        }

    }

    private fun parseImportFiles(data: Intent) {
        lifecycleScope.launch(Dispatchers.IO) {
            data.data.let { uri ->
                uri?.let {
                    activity?.contentResolver?.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                val pfd = uri?.let { activity?.contentResolver?.openFileDescriptor(it, "r") }
                pfd?.use {
                    FileInputStream(pfd.fileDescriptor).use { inputStream ->
                        try {
                            val restoreResult = restoreFromInputStream(
                                requireContext(),
                                inputStream
                            )
                            if (restoreResult) {
                                try {
                                    importInternally()
                                } catch (e: Exception) {
                                    loge("failed to read files", e)
                                }
                            } else {
                                logw("credentials couldn't be restored")
                            }
                        } catch (e: Error) {
                            loge("failed to read files", e)
                        }
                    }
                }
            }
        }
    }

    private suspend fun restoreFromInputStream(
        context: Context,
        contentStream: InputStream
    ): Boolean {
        return withContext(Dispatchers.IO) {
            var result = false
            var toBeRestoredZipFile: File? = null
            var extractedFilesDir: File? = null
            var restoredFilesDir: File? = null
            try {
                val filesDir = requireNotNull(context.filesDir.absoluteFile)

                toBeRestoredZipFile = File(filesDir.absolutePath + "/toBeRestored.zip")
                extractedFilesDir = File(filesDir.absolutePath + "/toBeRestoredDir")
                restoredFilesDir = File(filesDir.absolutePath + "/restored")

                restoredFilesDir.mkdir()
                extractedFilesDir.mkdir()
                contentStream.use { input ->
                    toBeRestoredZipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val pass = importPass?.toCharArray() ?: "".toCharArray()
                val preparedZipFile =
                    ZipFile(toBeRestoredZipFile.absolutePath, pass)
                importPass = null
                if (extractedFilesDir.exists())
                    preparedZipFile.extractAll(extractedFilesDir.absolutePath)

                // delete existing restored directory
                restoredFilesDir.deleteRecursively()

                val exists = extractedFilesDir.exists()
                if (exists) {
                    val toBeRestoredFolders = extractedFilesDir.listFiles()
                    toBeRestoredFolders?.forEach {
                        val contentFolderInData =
                            File(restoredFilesDir.absolutePath + "/" + it.name)
                        it.copyRecursively(contentFolderInData)
                    }
                    result = true
                }
            } catch (e: ZipException) {
                loge("couldn't open zip file", e)
                if (e.type == ZipException.Type.WRONG_PASSWORD) {
                    activity?.runOnUiThread {
                        view?.snackShort(getString(R.string.incorrect_password_import_message))
                    }
                } else {
                    throw e
                }
            } finally {
                val exists = extractedFilesDir?.exists() ?: false
                if (exists) {
                    extractedFilesDir?.deleteRecursively()
                }
            }
            return@withContext result
        }
    }

    private fun importInternally() {
        importDisposable.set(
            viewModel.importDbData(requireContext())
                .asyncToUiCompletable()
                .subscribe({
                    view?.snackShort(getString(R.string.credentials_imported_success))
                    goToWallet()
                }, {
                    loge("failed to import", it)
                    view?.snackShort(getString(R.string.credentials_imported_failed))
                    goToWallet()
                })
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(deleteAllDisposable.get(), importDisposable.get(), disposables)
    }

    companion object {
        private const val REQUEST_CODE_IMPORT = 2
        private const val REQUEST_CODE_CREATE_CREDENTIAL = 553
    }
}
