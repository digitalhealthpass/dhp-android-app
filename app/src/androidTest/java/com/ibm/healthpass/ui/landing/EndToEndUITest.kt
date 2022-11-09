package com.merative.healthpass.ui.landing
import org.junit.runner.RunWith
import org.junit.runners.Suite

/* Preconditions:
 *  1. Install QA1 Build
 *  2. for create credentials (you manually choose where you want it to be saved)
 *  3. for import credentials (you select where you saved it)
 *  4. It's easier to setup Google (i.e. Google Drive)
 *     account ahead of time before running automation test
 */

// Runs all automation tests.
@RunWith(Suite::class)
@Suite.SuiteClasses(
    ScanValidQR::class,
    ScanInvalidQR::class,
    ValidResults::class,
    TemperatureResults::class,
    DeleteOneCredential::class,
    EraseAllCredentials::class,
    DuplicateCredential::class,
    CreateCredentials::class,
    ImportCredentials::class,
)

class EndToEndUITest {
}