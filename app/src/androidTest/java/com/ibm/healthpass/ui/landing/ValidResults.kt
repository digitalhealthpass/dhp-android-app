package com.merative.healthpass.ui.landing

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ValidResults: BaseTest() {

    @Test
    fun hp_E2E_TC007() { // Reference name from Excel QA document
        //Scan and view details of Valid Result Credentials
        val qrCodeResult = "daily_hp_green"
        testUtils.selectFromPhotoLibrary(qrCodeResult,
            allow = false,
            addCredentials = true,
            checkCredentials = true,
            viewSourceCode = true,
            position = 2)
    }
}