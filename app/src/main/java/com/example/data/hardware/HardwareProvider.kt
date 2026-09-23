package com.example.data.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

class HardwareProvider(private val context: Context) {

    fun getHardwareInfo(): HardwareInfo {
        return HardwareInfo(
            displayInfo = getDisplayInfo(),
            cameraSummary = getCameraSummary(),
            sensors = getSensors()
        )
    }

    private fun getDisplayInfo(): DisplayInfo {
        val metrics = context.resources.displayMetrics
        val widthPx = metrics.widthPixels
        val heightPx = metrics.heightPixels
        val densityDpi = metrics.densityDpi
        val densityScale = metrics.density
        val widthDp = if (densityScale > 0) (widthPx / densityScale).toInt() else 0
        val heightDp = if (densityScale > 0) (heightPx / densityScale).toInt() else 0

        val bucket = when {
            densityDpi <= DisplayMetrics.DENSITY_LOW -> "ldpi (~120 dpi)"
            densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi (~160 dpi)"
            densityDpi <= DisplayMetrics.DENSITY_HIGH -> "hdpi (~240 dpi)"
            densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi (~320 dpi)"
            densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi (~480 dpi)"
            else -> "xxxhdpi (~640 dpi)"
        }

        var refreshRate: Float? = null
        var isHdr: Boolean? = null
        var isWideColor: Boolean? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = context.display
                refreshRate = display?.refreshRate
                isHdr = display?.isHdr
                isWideColor = display?.isWideColorGamut
            } else {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                val display = wm?.defaultDisplay
                @Suppress("DEPRECATION")
                refreshRate = display?.refreshRate
            }
        } catch (_: Exception) {
        }

        return DisplayInfo(
            widthPixels = widthPx,
            heightPixels = heightPx,
            widthDp = widthDp,
            heightDp = heightDp,
            densityDpi = densityDpi,
            densityBucket = bucket,
            refreshRateHz = refreshRate,
            isHdrSupported = isHdr,
            isWideColorGamut = isWideColor
        )
    }

    private fun getCameraSummary(): CameraSummary {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return CameraSummary(0, emptyList(), isSupported = false)

        return try {
            val cameraIds = cameraManager.cameraIdList
            val cameras = mutableListOf<CameraUnitInfo>()

            for (id in cameraIds) {
                try {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val facingInt = chars.get(CameraCharacteristics.LENS_FACING)
                    val facingStr = when (facingInt) {
                        CameraCharacteristics.LENS_FACING_BACK -> "Back (Primary)"
                        CameraCharacteristics.LENS_FACING_FRONT -> "Front (Selfie)"
                        CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                        else -> "Camera #$id"
                    }

                    val hwLevelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val hwLevelStr = when (hwLevelInt) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3 (Advanced)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
                        else -> "Standard"
                    }

                    val pixelArray = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                    val mp = pixelArray?.let { (it.width.toLong() * it.height.toLong()) / 1_000_000f }
                    val res = pixelArray?.let { "${it.width} × ${it.height}" }

                    val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val focals = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.toList()
                        ?: emptyList()

                    cameras.add(
                        CameraUnitInfo(
                            cameraId = id,
                            facing = facingStr,
                            hardwareLevel = hwLevelStr,
                            megapixelEstimate = mp,
                            resolutionPixels = res,
                            hasFlash = hasFlash,
                            focalLengths = focals
                        )
                    )
                } catch (_: Exception) {
                }
            }

            CameraSummary(
                totalCameras = cameras.size,
                cameras = cameras,
                isSupported = cameras.isNotEmpty()
            )
        } catch (_: SecurityException) {
            CameraSummary(0, emptyList(), isSupported = false)
        } catch (_: Exception) {
            CameraSummary(0, emptyList(), isSupported = false)
        }
    }

    private fun getSensors(): List<DetailedSensorInfo> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return emptyList()

        return try {
            val list = sensorManager.getSensorList(Sensor.TYPE_ALL)
            list.map { sensor ->
                val category = categorizeSensor(sensor.type)
                val typeName = sensor.stringType
                    ?.replace("android.sensor.", "")
                    ?.replace('_', ' ')
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Sensor Type ${sensor.type}"

                DetailedSensorInfo(
                    name = sensor.name.ifBlank { "Unknown Sensor" },
                    vendor = sensor.vendor.ifBlank { "Unknown Vendor" },
                    version = sensor.version,
                    typeName = typeName,
                    category = category,
                    powerMilliAmps = sensor.power,
                    resolution = sensor.resolution,
                    maximumRange = sensor.maximumRange,
                    minDelayMicroseconds = sensor.minDelay
                )
            }.sortedBy { it.category.ordinal }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun categorizeSensor(type: Int): SensorCategory {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR -> SensorCategory.MOTION

            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            Sensor.TYPE_ORIENTATION,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> SensorCategory.POSITION

            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY -> SensorCategory.ENVIRONMENT

            else -> SensorCategory.OTHER
        }
    }
}
