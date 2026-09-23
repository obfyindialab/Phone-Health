package com.example.data.battery

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Legacy monitor that delegates to [BatteryRepository].
 */
@Deprecated(
    message = "Use BatteryRepository directly for battery telemetry operations",
    replaceWith = ReplaceWith("BatteryRepository(context)", "com.example.data.battery.BatteryRepository")
)
class BatteryMonitor(private val context: Context) {

    private val repository: BatteryRepository = BatteryRepository(context)

    fun getBatteryInfoFlow(): Flow<BatteryInfo> = repository.getBatteryInfoFlow()

    fun readCurrentBatteryInfo(): BatteryInfo = repository.getCurrentBatteryInfo()
}
