# QR Coder library version 0.1  

It provides simple Fragment that will display the camera view, scanning QR functionality, and methods to decode a provides Bitmap (Image) to a string.

The library will handle requesting the permission. If the use don't grant the permission, the SDK will show an error message.

Current version 0.1 June 24, 2020

## Requirements:
    minSdkVersion 28
    Kotlin '1.4.32'
    material design version = "1.4.0-rc01" //to show an error

    ext {
        app_compat = "1.3.0"
        mat_ver = "1.4.0-rc01"
        kotlin_version = '1.4.32'
        ktx_extension = "1.5.0"
        nav_version = "2.4.0-alpha01"
        fragment = "1.4.0-alpha01"
        zxing_code = "3.4.1"
        legacy_support = "1.0.0"
        zip4 = "2.6.4"
        google_service = "4.3.8"
    }

## Usage:

You need to add the library in your gradle file.
    //add QR Coder project
    implementation project(path: ':QRCoder')

### QRCoderDecoder class
    This will decode the bitmap and return the String(QR code string) parsed from the Image bitmap
    
    fun decodeBitmap(
        bitmap: Bitmap,
        onSuccessListener: Consumer<String>?,
        onErrorListener: Consumer<Exception>?
    )

### QRCoderEncoder class
    fun encode(
        qrCodeJson: String,
        size: Int = 300,
        color: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap

    
### To receive the QR code string, you need to something like

        setFragmentResultListener(
            ScanFragment.KEY_RESULT
        ) { _, bundle ->
            val qrCredentialJson = bundle.getString(ScanFragment.KEY_QR_SCHEMA_JSON, "")
            handleData(qrCredentialJsonString)
        }


## Customization:

- If you want to change the UI, you can override file "fragment_scan.xml", however you must keep the view IDs the same.
- You can also override the button colors, strings values by using same ids in your "res" respective files, but you must use the same IDs.
