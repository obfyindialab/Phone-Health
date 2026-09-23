package com.example.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository for streaming real-time battery telemetry using Android's BatteryManager
 * and IntentFilter(Intent.ACTION_BATTERY_CHANGED).
 */
interface BatteryRepository {

    /**
     * Flow emitting real-time battery telemetry via IntentFilter(Intent.ACTION_BATTERY_CHANGED).
     */
    val batteryFlow: Flow<BatteryInfo>
        get() = getBatteryInfoFlow()

    /**
     * Streams real-time battery telemetry (level, status, temperature, health, voltage, etc.)
     * via IntentFilter(Intent.ACTION_BATTERY_CHANGED) broadcast emissions.
     */
    fun getBatteryInfoFlow(): Flow<BatteryInfo>

    /**
     * Synchronously reads the latest sticky battery intent and BatteryManager properties.
     */
    fun getCurrentBatteryInfo(): BatteryInfo

    companion object {
        operator fun invoke(context: Context): BatteryRepository =
            DefaultBatteryRepository(context.applicationContext)
    }
}

/**
 * Default implementation of [BatteryRepository] querying [BatteryManager]
 * and listening for [Intent.ACTION_BATTERY_CHANGED].
 */
class DefaultBatteryRepository(
    private val context: Context,
    private val batteryManager: BatteryManager? = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
) : BatteryRepository {

    override val batteryFlow: Flow<BatteryInfo>
        get() = getBatteryInfoFlow()

    override fun getBatteryInfoFlow(): Flow<BatteryInfo> = callbackFlow {
        // Emit initial sticky reading immediately to subscribers
        trySend(getCurrentBatteryInfo())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    trySend(parseBatteryIntent(intent))
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
                // Ignore if receiver already unregistered
            }
        }
    }

    override fun getCurrentBatteryInfo(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return parseBatteryIntent(intent)
    }

    private fun parseBatteryIntent(intent: Intent?): BatteryInfo {
        if (intent == null) {
            return fallbackBatteryInfo()
        }

        // 1. Battery Level calculation
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val rawScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (rawLevel >= 0 && rawScale > 0) {
            ((rawLevel.toFloat() / rawScale.toFloat()) * 100).toInt()
        } else {
            val cap = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (cap != null && cap in 0..100) cap else null
        }

        // 2. Battery Status (Charging, Discharging, Full, Not Charging)
        val statusInt = intent.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        )
        val status = BatteryStatus.fromInt(statusInt)

        // 3. Plugged Source
        val pluggedInt = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val pluggedSource = PluggedSource.fromInt(pluggedInt)

        // 4. Battery Health
        val healthInt = intent.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        )
        val health = BatteryHealthStatus.fromInt(healthInt)

        // 5. Temperature in Celsius (Android reports in tenths of a degree Celsius)
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperatureCelsius = if (rawTemp > 0) rawTemp / 10f else null

        // 6. Voltage in mV
        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltageMilliVolts = if (rawVoltage > 0) rawVoltage else null

        // 7. Technology & Presence
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() }
        val isPresent = if (intent.hasExtra(BatteryManager.EXTRA_PRESENT)) {
            intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true)
        } else null

        // 8. Properties from BatteryManager hardware registers
        val chargeCounter = safeGetLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val currentNow = safeGetLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAverage = safeGetLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val energyCounter = safeGetLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        val capacityPercent = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?.takeIf { it in 0..100 }

        return BatteryInfo(
            percentage = percentage,
            status = status,
            pluggedSource = pluggedSource,
            health = health,
            temperatureCelsius = temperatureCelsius,
            voltageMilliVolts = voltageMilliVolts,
            technology = tech,
            capacityPercent = capacityPercent,
            chargeCounterMicroAmpHours = chargeCounter,
            currentNowMicroAmps = currentNow,
            currentAverageMicroAmps = currentAverage,
            energyCounterNanoWattHours = energyCounter,
            isPresent = isPresent
        )
    }

    private fun safeGetLongProperty(propertyId: Int): Long? {
        val bm = batteryManager ?: return null
        return try {
            val value = bm.getLongProperty(propertyId)
            // Long.MIN_VALUE represents property not supported on Android
            if (value == Long.MIN_VALUE || value == 0L) null else value
        } catch (_: Exception) {
            null
        }
    }

    private fun fallbackBatteryInfo(): BatteryInfo {
        val capacity = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?.takeIf { it in 0..100 }
        return BatteryInfo(
            percentage = capacity,
            status = BatteryStatus.UNKNOWN,
            pluggedSource = PluggedSource.UNKNOWN,
            health = BatteryHealthStatus.UNKNOWN,
            temperatureCelsius = null,
            voltageMilliVolts = null,
            technology = null,
            capacityPercent = capacity,
            chargeCounterMicroAmpHours = null,
            currentNowMicroAmps = null,
            currentAverageMicroAmps = null,
            energyCounterNanoWattHours = null,
            isPresent = null
        )
    }
}
