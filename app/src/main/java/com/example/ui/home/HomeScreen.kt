package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import com.example.data.battery.BatteryStatus
import com.example.data.settings.TemperatureUnit
import com.example.ui.components.DashboardMetricCard
import com.example.ui.components.HealthGaugeCard
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.RoseAlert

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBattery: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToHardware: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        // Responsive columns: 2 columns on phones (<600dp), adaptive on larger screens
        val gridColumns = if (maxWidth >= 600.dp) {
            GridCells.Adaptive(minSize = 180.dp)
        } else {
            GridCells.Fixed(2)
        }

        LazyVerticalGrid(
            columns = gridColumns,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("home_metric_grid"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                ScreenHeader(
                    title = "Phone Health",
                    subtitle = "${state.deviceInfo.manufacturer} ${state.deviceInfo.model}",
                    onRefresh = { viewModel.refresh() }
                )
            }

            // Health Summary Gauge
            item(span = { GridItemSpan(maxLineSpan) }) {
                HealthGaugeCard(summary = state.healthSummary)
            }

            // Quick Navigation Row
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickNavRow(
                    onBatteryClick = onNavigateToBattery,
                    onPerformanceClick = onNavigateToPerformance,
                    onStorageClick = onNavigateToStorage,
                    onHardwareClick = onNavigateToHardware
                )
            }

            // Key Metrics Section Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Key Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Battery Metric Card (Key Metric)
            if (state.cardVisibility.showBattery) {
                item {
                    val battery = state.batteryInfo
                    val level = battery.percentage
                    val temp = if (state.tempUnit == TemperatureUnit.CELSIUS) {
                        battery.temperatureCelsius?.let { String.format("%.1f°C", it) }
                    } else {
                        battery.temperatureFahrenheit?.let { String.format("%.1f°F", it) }
                    } ?: "N/A"

                    val isCharging = battery.status == BatteryStatus.CHARGING
                    val statusText = when (battery.status) {
                        BatteryStatus.CHARGING -> "Charging (${battery.pluggedSource.name})"
                        BatteryStatus.FULL -> "Fully Charged"
                        BatteryStatus.DISCHARGING -> "Discharging"
                        BatteryStatus.NOT_CHARGING -> "Not Charging"
                        else -> "Active"
                    }

                    val badgeText = when {
                        isCharging -> "CHARGING"
                        battery.health.name != "UNKNOWN" -> battery.health.name
                        else -> null
                    }

                    DashboardMetricCard(
                        title = "Battery",
                        value = if (level != null) "$level%" else "N/A",
                        subtitle = "$statusText • $temp",
                        icon = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        progress = level?.let { it / 100f },
                        accentColor = when {
                            level == null -> MaterialTheme.colorScheme.onSurfaceVariant
                            level >= 50 -> EmeraldTeal
                            level >= 20 -> AmberWarning
                            else -> RoseAlert
                        },
                        badgeText = badgeText,
                        testTag = "home_battery_card",
                        onClick = onNavigateToBattery
                    )
                }
            }

            // Memory (RAM) Metric Card (Key Metric)
            if (state.cardVisibility.showMemory) {
                item {
                    val mem = state.memoryInfo
                    val usedStr = String.format("%.1f", mem.usedGb)
                    val totalStr = String.format("%.1f", mem.totalGb)
                    val freeStr = String.format("%.1f", mem.availableGb)
                    val lowMemBadge = if (mem.isLowMemory) "LOW RAM" else "${mem.usagePercentage}%"

                    DashboardMetricCard(
                        title = "RAM Memory",
                        value = "${mem.usagePercentage}%",
                        subtitle = "$usedStr / $totalStr GB ($freeStr GB free)",
                        icon = Icons.Default.Memory,
                        progress = mem.usagePercentage / 100f,
                        accentColor = when {
                            mem.isLowMemory -> RoseAlert
                            mem.usagePercentage < 80 -> EmeraldTeal
                            else -> AmberWarning
                        },
                        badgeText = lowMemBadge,
                        testTag = "home_ram_card",
                        onClick = onNavigateToPerformance
                    )
                }
            }

            // Storage Metric Card (Key Metric)
            if (state.cardVisibility.showStorage) {
                item {
                    val storage = state.storageInfo.internalStorage
                    val usedStr = String.format("%.1f", storage.usedGb)
                    val totalStr = String.format("%.1f", storage.totalGb)
                    val freeStr = String.format("%.1f", storage.freeGb)

                    DashboardMetricCard(
                        title = "Storage",
                        value = "${storage.usagePercentage}%",
                        subtitle = "$usedStr / $totalStr GB ($freeStr GB free)",
                        icon = Icons.Default.Storage,
                        progress = storage.usagePercentage / 100f,
                        accentColor = when {
                            storage.usagePercentage < 75 -> CyanAccent
                            storage.usagePercentage < 90 -> AmberWarning
                            else -> RoseAlert
                        },
                        badgeText = "${String.format("%.0f", storage.freeGb)} GB FREE",
                        testTag = "home_storage_card",
                        onClick = onNavigateToStorage
                    )
                }
            }

            // CPU & Processor Card
            if (state.cardVisibility.showCpu) {
                item {
                    val cpu = state.cpuInfo
                    DashboardMetricCard(
                        title = "Processor",
                        value = "${cpu.coreCount} Cores",
                        subtitle = "${cpu.primaryAbi} • Up: ${cpu.formattedElapsedUptime}",
                        icon = Icons.Default.Speed,
                        accentColor = CyanAccent,
                        badgeText = "${cpu.coreCount} CORES",
                        testTag = "home_cpu_card",
                        onClick = onNavigateToPerformance
                    )
                }
            }

            // Device Info Card
            if (state.cardVisibility.showDevice) {
                item {
                    val dev = state.deviceInfo
                    DashboardMetricCard(
                        title = "Device Specs",
                        value = dev.model,
                        subtitle = "${dev.manufacturer} • Android ${dev.androidVersion}",
                        icon = Icons.Default.Devices,
                        accentColor = MaterialTheme.colorScheme.primary,
                        badgeText = "API ${dev.sdkInt}",
                        testTag = "home_device_card",
                        onClick = onNavigateToHardware
                    )
                }
            }

            // Network Card
            if (state.cardVisibility.showNetwork) {
                item {
                    val net = state.networkInfo
                    val isOnline = net.isConnected
                    val speed = net.downstreamSpeedFormatted?.let { "• $it" } ?: ""
                    val netSubtitle = if (net.isMetered) "Metered link $speed" else "Unmetered link $speed"

                    DashboardMetricCard(
                        title = "Network",
                        value = if (isOnline) net.connectionType.displayName else "Offline",
                        subtitle = netSubtitle,
                        icon = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                        accentColor = if (isOnline) EmeraldTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                        badgeText = if (isOnline) "ONLINE" else "OFFLINE",
                        testTag = "home_network_card",
                        onClick = onNavigateToNetwork
                    )
                }
            }

            // Bottom Spacing Item
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickNavRow(
    onBatteryClick: () -> Unit,
    onPerformanceClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHardwareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickNavChip(
            title = "Battery",
            icon = Icons.Default.BatteryChargingFull,
            onClick = onBatteryClick,
            modifier = Modifier.weight(1f)
        )
        QuickNavChip(
            title = "Speed",
            icon = Icons.Default.Speed,
            onClick = onPerformanceClick,
            modifier = Modifier.weight(1f)
        )
        QuickNavChip(
            title = "Storage",
            icon = Icons.Default.Storage,
            onClick = onStorageClick,
            modifier = Modifier.weight(1f)
        )
        QuickNavChip(
            title = "Hardware",
            icon = Icons.Default.Devices,
            onClick = onHardwareClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickNavChip(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.5.sp
            )
        }
    }
}
