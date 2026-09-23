package com.example.ui.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.battery.BatteryHealthStatus
import com.example.data.battery.BatteryStatus
import com.example.data.battery.PluggedSource
import com.example.data.settings.TemperatureUnit
import com.example.ui.components.InfoDisclaimerCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TelemetryRow
import com.example.ui.components.TelemetrySection
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.RoseAlert

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val battery = state.batteryInfo

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Battery Telemetry",
                    subtitle = "Real-time hardware power diagnostics"
                )
            }

            // Big Battery Status Card
            item {
                BatteryHeroCard(
                    percentage = battery.percentage,
                    status = battery.status,
                    pluggedSource = battery.pluggedSource,
                    health = battery.health
                )
            }

            // Core Power & Thermal Section
            item {
                TelemetrySection(
                    title = "Core Power Diagnostics",
                    icon = Icons.Default.Bolt
                ) {
                    val temp = if (state.tempUnit == TemperatureUnit.CELSIUS) {
                        battery.temperatureCelsius?.let { String.format("%.1f °C", it) }
                    } else {
                        battery.temperatureFahrenheit?.let { String.format("%.1f °F", it) }
                    }

                    TelemetryRow(label = "Battery Percentage", value = battery.percentage?.let { "$it%" })
                    TelemetryRow(label = "Power Status", value = battery.status.name.lowercase().replaceFirstChar { it.uppercase() })
                    TelemetryRow(label = "Charging Source", value = battery.pluggedSource.name)
                    TelemetryRow(label = "Battery Temperature", value = temp)
                    TelemetryRow(label = "Battery Voltage", value = battery.voltageVolts?.let { String.format("%.3f V (%d mV)", it, battery.voltageMilliVolts) })
                    TelemetryRow(label = "Cell Technology", value = battery.technology, showDivider = false)
                }
            }

            // Android Health Reporting Section
            item {
                TelemetrySection(
                    title = "Android System Health Report",
                    icon = Icons.Default.BatteryAlert
                ) {
                    val healthDesc = when (battery.health) {
                        BatteryHealthStatus.GOOD -> "Good (Reported by OS kernel)"
                        BatteryHealthStatus.OVERHEAT -> "Overheat Alert"
                        BatteryHealthStatus.DEAD -> "Dead / Replace battery"
                        BatteryHealthStatus.OVER_VOLTAGE -> "Over Voltage Warning"
                        BatteryHealthStatus.UNSPECIFIED_FAILURE -> "Unspecified Kernel Failure"
                        BatteryHealthStatus.COLD -> "Cold Thermal Warning"
                        BatteryHealthStatus.UNKNOWN -> "Unknown / Unreported"
                    }

                    TelemetryRow(label = "Android Health Flag", value = healthDesc)
                    TelemetryRow(label = "Battery Present In Unit", value = battery.isPresent?.let { if (it) "Yes" else "No" })
                    TelemetryRow(
                        label = "Reported Capacity Indicator",
                        value = battery.capacityPercent?.let { "$it%" }
                    )
                    TelemetryRow(
                        label = "Coulomb Counter (Charge)",
                        value = battery.chargeCounterMicroAmpHours?.let {
                            String.format("%.1f mAh (%d µAh)", it / 1000f, it)
                        },
                        showDivider = false
                    )
                }
            }

            // Hardware Telemetry & Current Flow Section
            item {
                TelemetrySection(
                    title = "Instantaneous Current & Energy",
                    icon = Icons.Default.ElectricMeter
                ) {
                    TelemetryRow(
                        label = "Current Now (Instantaneous)",
                        value = battery.currentNowMicroAmps?.let {
                            val mA = it / 1000f
                            String.format("%.1f mA (%d µA)", mA, it)
                        }
                    )
                    TelemetryRow(
                        label = "Average Current Draw",
                        value = battery.currentAverageMicroAmps?.let {
                            val mA = it / 1000f
                            String.format("%.1f mA (%d µA)", mA, it)
                        }
                    )
                    TelemetryRow(
                        label = "Energy Counter",
                        value = battery.energyCounterNanoWattHours?.let {
                            String.format("%.2f mWh (%d nWh)", it / 1_000_000f, it)
                        },
                        showDivider = false
                    )
                }
            }

            // Informative Note
            item {
                InfoDisclaimerCard(
                    text = "Android security sandboxing only exposes current, energy counter, and microamp metrics when the device power supply management IC (PMIC) kernel drivers make them available to user-space applications. If not exposed by your device manufacturer, they will correctly display as unavailable."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BatteryHeroCard(
    percentage: Int?,
    status: BatteryStatus,
    pluggedSource: PluggedSource,
    health: BatteryHealthStatus,
    modifier: Modifier = Modifier
) {
    val levelColor = when {
        percentage == null -> MaterialTheme.colorScheme.onSurfaceVariant
        percentage >= 50 -> EmeraldTeal
        percentage >= 20 -> AmberWarning
        else -> RoseAlert
    }

    val icon = when (status) {
        BatteryStatus.CHARGING -> Icons.Default.BatteryChargingFull
        BatteryStatus.FULL -> Icons.Default.BatteryFull
        else -> Icons.Default.Power
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("battery_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CHARGE LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = percentage?.let { "$it%" } ?: "N/A",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = levelColor,
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (status) {
                        BatteryStatus.CHARGING -> "Charging via ${pluggedSource.name}"
                        BatteryStatus.FULL -> "Fully Charged"
                        BatteryStatus.DISCHARGING -> "Discharging on Battery"
                        BatteryStatus.NOT_CHARGING -> "Plugged, Not Charging"
                        BatteryStatus.UNKNOWN -> "Status Unknown"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(levelColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Battery Status",
                    tint = levelColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
