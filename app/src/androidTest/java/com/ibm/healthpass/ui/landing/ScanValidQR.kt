package com.merative.healthpass.ui.landing

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.R
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScanValidQR: BaseTest() {

    private val isEmulator = testUtils.isEmulator()

    @Test
    fun hp_E2E_TC003() { // Reference name from Excel QA document
        val qrCodeResult = "test_result_positive"
        Espresso.onView(ViewMatchers.withId(R.id.terms_edit_text)).waitUntilVisible(isLoading = false, createCredentials = false)
        testUtils.agreeTerms()
        testUtils.completeTutorial()
        //scan Valid QR Code
        if (isEmulator) {
            testUtils.selectFromPhotoLibrary(qrCodeResult,
                allow = true,
                addCredentials = true,
                checkCredentials = true,
                viewSourceCode = true,
                position = 1)
        } else {
            testUtils.scanQRCode(true)
        }
    }
}