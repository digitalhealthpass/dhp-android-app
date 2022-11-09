package com.merative.healthpass.ui.landing

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TemperatureResults: BaseTest() {

    @Test
    fun hp_E2E_TC008() { // Reference name from Excel QA document
        //Scan and view details of Temperature Result Credentials
        val qrCodeResult = "temp_check_normal"
        testUtils.selectFromPhotoLibrary(qrCodeResult,
                allow = false,
                addCredentials = true,
                checkCredentials = true,
                viewSourceCode = true,
                position = 3)
    }
}