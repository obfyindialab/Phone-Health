package com.example.data.hardware

data class DisplayInfo(
    val widthPixels: Int,
    val heightPixels: Int,
    val widthDp: Int,
    val heightDp: Int,
    val densityDpi: Int,
    val densityBucket: String,
    val refreshRateHz: Float?,
    val isHdrSupported: Boolean?,
    val isWideColorGamut: Boolean?
) {
    val resolutionFormatted: String
        get() = "${widthPixels} × ${heightPixels} px"

    val refreshRateFormatted: String
        get() = refreshRateHz?.let { String.format("%.0f Hz", it) } ?: "Not available on this device"
}

data class CameraUnitInfo(
    val cameraId: String,
    val facing: String,
    val hardwareLevel: String,
    val megapixelEstimate: Float?,
    val resolutionPixels: String?,
    val hasFlash: Boolean,
    val focalLengths: List<Float>
)

data class CameraSummary(
    val totalCameras: Int,
    val cameras: List<CameraUnitInfo>,
    val isSupported: Boolean
)

enum class SensorCategory(val displayName: String) {
    MOTION("Motion"),
    POSITION("Position"),
    ENVIRONMENT("Environment"),
    OTHER("Specialized")
}

data class DetailedSensorInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val typeName: String,
    val category: SensorCategory,
    val powerMilliAmps: Float,
    val resolution: Float,
    val maximumRange: Float,
    val minDelayMicroseconds: Int
)

data class HardwareInfo(
    val displayInfo: DisplayInfo,
    val cameraSummary: CameraSummary,
    val sensors: List<DetailedSensorInfo>
)
