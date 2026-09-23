package com.example.data.device

import android.os.Build

data class DeviceInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val deviceName: String,
    val product: String,
    val hardware: String,
    val board: String,
    val bootloader: String,
    val androidVersion: String,
    val sdkInt: Int,
    val codename: String,
    val securityPatch: String?,
    val buildId: String,
    val fingerprint: String
)

class DeviceProvider {
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            deviceName = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE,
            board = Build.BOARD,
            bootloader = Build.BOOTLOADER,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            codename = Build.VERSION.CODENAME,
            securityPatch = Build.VERSION.SECURITY_PATCH.takeIf { !it.isNullOrBlank() },
            buildId = Build.ID,
            fingerprint = Build.FINGERPRINT
        )
    }
}
