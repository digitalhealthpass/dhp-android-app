package com.merative.healthpass.ui.landing

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ScanInvalidQR: BaseTest() {

    private val isEmulator = testUtils.isEmulator()

    @Test
    fun hp_E2E_TC004() { // Reference name from Excel QA document
        //scan Invalid QR Code
        val qrCodeResult = "tampered_test_result"
        if (isEmulator) {
            testUtils.invalidScan(qrCodeResult, allow = false, addCredentials = false)
        } else {
            // Please make sure QR code is invalid
            testUtils.scanQRCode(false)
        }
    }
}