package com.example.data.thermal

import android.content.Context
import android.os.Build
import android.os.PowerManager

class ThermalProvider(private val context: Context) {

    private val powerManager: PowerManager? by lazy {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }

    fun getThermalStatus(): ThermalThrottleStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ThermalThrottleStatus.UNAVAILABLE
        }

        val pm = powerManager ?: return ThermalThrottleStatus.UNAVAILABLE

        return try {
            when (pm.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalThrottleStatus.NONE
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalThrottleStatus.LIGHT
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalThrottleStatus.MODERATE
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalThrottleStatus.SEVERE
                PowerManager.THERMAL_STATUS_CRITICAL -> ThermalThrottleStatus.CRITICAL
                PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalThrottleStatus.EMERGENCY
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalThrottleStatus.SHUTDOWN
                else -> ThermalThrottleStatus.UNAVAILABLE
            }
        } catch (_: Exception) {
            ThermalThrottleStatus.UNAVAILABLE
        }
    }
}
