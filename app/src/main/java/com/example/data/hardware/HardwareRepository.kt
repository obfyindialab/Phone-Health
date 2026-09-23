package com.example.data.hardware

import android.content.Context
import android.os.Build
import com.example.data.device.DeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Data model representing device build details extracted from Android [Build] constants.
 */
data class BuildDetails(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val brand: String = "",
    val deviceName: String = "",
    val product: String = "",
    val hardware: String = "",
    val board: String = "",
    val bootloader: String = "",
    val codename: String = "",
    val securityPatch: String? = null,
    val buildId: String = "",
    val fingerprint: String = "",
    val supportedAbis: List<String> = emptyList()
) {
    val versionFormatted: String
        get() = "Android $androidVersion (API $sdkInt)"

    val deviceFormatted: String
        get() = "$manufacturer $model"
}

/**
 * Repository interface for retrieving device build details, hardware specifications,
 * display properties, camera features, and sensors.
 */
interface HardwareRepository {

    /**
     * Fetches build details like manufacturer, model, and Android version using [Build] constants.
     */
    fun getBuildDetails(): BuildDetails

    /**
     * Streams build details as a Flow.
     */
    fun getBuildDetailsFlow(): Flow<BuildDetails>

    /**
     * Fetches device identity representation.
     */
    fun getDeviceInfo(): DeviceInfo

    /**
     * Streams device identity representation as a Flow.
     */
    fun getDeviceInfoFlow(): Flow<DeviceInfo>

    /**
     * Fetches comprehensive hardware diagnostics (display, cameras, sensors).
     */
    fun getHardwareInfo(): HardwareInfo

    /**
     * Streams hardware diagnostics as a Flow.
     */
    fun getHardwareInfoFlow(): Flow<HardwareInfo>

    companion object {
        operator fun invoke(context: Context): HardwareRepository =
            DefaultHardwareRepository(context.applicationContext)
    }
}

/**
 * Default implementation of [HardwareRepository] querying Android [Build] constants
 * and [HardwareProvider].
 */
class DefaultHardwareRepository(
    private val context: Context,
    private val hardwareProvider: HardwareProvider = HardwareProvider(context)
) : HardwareRepository {

    override fun getBuildDetails(): BuildDetails {
        return BuildDetails(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            deviceName = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE,
            board = Build.BOARD,
            bootloader = Build.BOOTLOADER,
            codename = Build.VERSION.CODENAME,
            securityPatch = Build.VERSION.SECURITY_PATCH.takeIf { !it.isNullOrBlank() },
            buildId = Build.ID,
            fingerprint = Build.FINGERPRINT,
            supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        )
    }

    override fun getBuildDetailsFlow(): Flow<BuildDetails> = flow {
        emit(getBuildDetails())
    }

    override fun getDeviceInfo(): DeviceInfo {
        val build = getBuildDetails()
        return DeviceInfo(
            manufacturer = build.manufacturer,
            brand = build.brand,
            model = build.model,
            deviceName = build.deviceName,
            product = build.product,
            hardware = build.hardware,
            board = build.board,
            bootloader = build.bootloader,
            androidVersion = build.androidVersion,
            sdkInt = build.sdkInt,
            codename = build.codename,
            securityPatch = build.securityPatch,
            buildId = build.buildId,
            fingerprint = build.fingerprint
        )
    }

    override fun getDeviceInfoFlow(): Flow<DeviceInfo> = flow {
        emit(getDeviceInfo())
    }

    override fun getHardwareInfo(): HardwareInfo {
        return hardwareProvider.getHardwareInfo()
    }

    override fun getHardwareInfoFlow(): Flow<HardwareInfo> = flow {
        emit(getHardwareInfo())
    }
}
