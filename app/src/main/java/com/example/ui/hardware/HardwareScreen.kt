package com.example.ui.hardware

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.hardware.DetailedSensorInfo
import com.example.data.hardware.SensorCategory
import com.example.ui.components.InfoDisclaimerCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TelemetryRow
import com.example.ui.components.TelemetrySection
import com.example.ui.components.UnavailableBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal

@Composable
fun HardwareScreen(
    viewModel: HardwareViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val build = state.buildDetails
    val dev = state.deviceInfo
    val hw = state.hardwareInfo
    val display = hw.displayInfo
    val camera = hw.cameraSummary
    val sensors = hw.sensors

    val filteredSensors = if (state.selectedSensorCategory == null) {
        sensors
    } else {
        sensors.filter { it.category == state.selectedSensorCategory }
    }

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
                    title = "Hardware & Sensors",
                    subtitle = "System identity, display, cameras & sensors",
                    onRefresh = { viewModel.refresh() }
                )
            }

            // Build Identity Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Devices,
                                    contentDescription = "Device Identity",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${build.manufacturer} ${build.model}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Android ${build.androidVersion} • API ${build.sdkInt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Device Identity
            item {
                TelemetrySection(
                    title = "Device Identity & OS",
                    icon = Icons.Default.Devices
                ) {
                    TelemetryRow(label = "Manufacturer", value = build.manufacturer)
                    TelemetryRow(label = "Brand", value = build.brand.ifBlank { dev.brand })
                    TelemetryRow(label = "Model", value = build.model)
                    TelemetryRow(label = "Device Name", value = build.deviceName.ifBlank { dev.deviceName })
                    TelemetryRow(label = "Product Name", value = build.product.ifBlank { dev.product })
                    TelemetryRow(label = "Hardware Board", value = "${build.hardware.ifBlank { dev.hardware }} (${build.board.ifBlank { dev.board }})")
                    TelemetryRow(label = "Bootloader", value = build.bootloader.ifBlank { dev.bootloader })
                    TelemetryRow(label = "Android Version", value = "Android ${build.androidVersion} (${build.codename.ifBlank { dev.codename }})")
                    TelemetryRow(label = "API Level (SDK)", value = "${build.sdkInt}")
                    if (build.supportedAbis.isNotEmpty()) {
                        TelemetryRow(label = "Instruction Set (ABI)", value = build.supportedAbis.joinToString(", "))
                    }
                    TelemetryRow(label = "Security Patch Level", value = build.securityPatch ?: dev.securityPatch)
                    TelemetryRow(label = "Build ID", value = build.buildId.ifBlank { dev.buildId }, showDivider = false)
                }
            }

            // Display Section
            item {
                TelemetrySection(
                    title = "Display & Screen",
                    icon = Icons.Default.Tv
                ) {
                    TelemetryRow(label = "Physical Resolution", value = display.resolutionFormatted)
                    TelemetryRow(label = "Screen Density", value = "${display.densityDpi} DPI (${display.densityBucket})")
                    TelemetryRow(label = "Screen Dimensions (dp)", value = "${display.widthDp} × ${display.heightDp} dp")
                    TelemetryRow(label = "Refresh Rate", value = display.refreshRateFormatted)
                    TelemetryRow(label = "HDR Screen Support", value = display.isHdrSupported?.let { if (it) "Supported" else "Not Supported" })
                    TelemetryRow(label = "Wide Color Gamut", value = display.isWideColorGamut?.let { if (it) "Supported" else "Not Supported" }, showDivider = false)
                }
            }

            // Camera Capabilities Section
            item {
                TelemetrySection(
                    title = "Camera Hardware Capabilities",
                    icon = Icons.Default.CameraAlt
                ) {
                    if (camera.isSupported && camera.cameras.isNotEmpty()) {
                        TelemetryRow(label = "Cameras Exposed", value = "${camera.totalCameras} Camera Units")
                        camera.cameras.forEachIndexed { index, cam ->
                            val mpText = cam.megapixelEstimate?.let { String.format("%.1f MP", it) } ?: "N/A"
                            val resText = cam.resolutionPixels?.let { "($it)" } ?: ""
                            val flashText = if (cam.hasFlash) "Flash Available" else "No Flash"
                            val focals = if (cam.focalLengths.isNotEmpty()) {
                                cam.focalLengths.joinToString(", ") { String.format("%.1fmm", it) }
                            } else "Standard"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${cam.facing} (ID: ${cam.cameraId})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = cam.hardwareLevel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Resolution: $mpText $resText • $flashText • Focal: $focals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (index < camera.cameras.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 0.7.dp
                                    )
                                }
                            }
                        }
                    } else {
                        UnavailableBadge(text = "Camera telemetry not exposed or restricted on this device")
                    }
                }
            }

            // Sensors Section Header + Category Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "On-Device Physical Sensors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "${sensors.size} Detected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedSensorCategory == null,
                            onClick = { viewModel.selectSensorCategory(null) },
                            label = { Text("All (${sensors.size})") }
                        )
                        SensorCategory.entries.forEach { cat ->
                            val count = sensors.count { it.category == cat }
                            FilterChip(
                                selected = state.selectedSensorCategory == cat,
                                onClick = { viewModel.selectSensorCategory(cat) },
                                label = { Text("${cat.displayName} ($count)") }
                            )
                        }
                    }
                }
            }

            // Sensor Items
            if (filteredSensors.isEmpty()) {
                item {
                    UnavailableBadge(
                        text = "No sensors detected in this category on this device",
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredSensors) { sensor ->
                    SensorCard(sensor = sensor)
                }
            }

            item {
                InfoDisclaimerCard(
                    text = "Sensors are enumerated via Android's SensorManager system service. Power consumption (mA) and resolution values are vendor-calibrated specifications reported by the device firmware."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SensorCard(
    sensor: DetailedSensorInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sensor_item_${sensor.name.replace(' ', '_')}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = sensor.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vendor: ${sensor.vendor} • Type: ${sensor.typeName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Power: ${String.format("%.3f mA", sensor.powerMilliAmps)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Max Range: ${sensor.maximumRange}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
