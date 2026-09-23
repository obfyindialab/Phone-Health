package com.example.data.battery

import android.os.BatteryManager

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN;

    companion object {
        fun fromInt(status: Int): BatteryStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> NOT_CHARGING
            else -> UNKNOWN
        }
    }
}

enum class PluggedSource {
    AC,
    USB,
    WIRELESS,
    DOCK,
    BATTERY,
    UNKNOWN;

    companion object {
        fun fromInt(plugged: Int): PluggedSource = when {
            plugged and BatteryManager.BATTERY_PLUGGED_AC != 0 -> AC
            plugged and BatteryManager.BATTERY_PLUGGED_USB != 0 -> USB
            plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0 -> WIRELESS
            plugged and 8 != 0 -> DOCK // BATTERY_PLUGGED_DOCK = 8 in newer SDKs
            plugged == 0 -> BATTERY
            else -> UNKNOWN
        }
    }
}

enum class BatteryHealthStatus {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN;

    companion object {
        fun fromInt(health: Int): BatteryHealthStatus = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> COLD
            else -> UNKNOWN
        }
    }
}

data class BatteryInfo(
    val percentage: Int?,
    val status: BatteryStatus,
    val pluggedSource: PluggedSource,
    val health: BatteryHealthStatus,
    val temperatureCelsius: Float?,
    val voltageMilliVolts: Int?,
    val technology: String? = null,
    val capacityPercent: Int? = null,
    val chargeCounterMicroAmpHours: Long? = null,
    val currentNowMicroAmps: Long? = null,
    val currentAverageMicroAmps: Long? = null,
    val energyCounterNanoWattHours: Long? = null,
    val isPresent: Boolean? = true
) {
    val level: Int?
        get() = percentage

    val temperatureFahrenheit: Float?
        get() = temperatureCelsius?.let { (it * 9f / 5f) + 32f }

    val voltageVolts: Float?
        get() = voltageMilliVolts?.let { it / 1000f }

    val currentNowMilliAmps: Float?
        get() = currentNowMicroAmps?.let { it / 1000f }

    val currentAverageMilliAmps: Float?
        get() = currentAverageMicroAmps?.let { it / 1000f }
}
