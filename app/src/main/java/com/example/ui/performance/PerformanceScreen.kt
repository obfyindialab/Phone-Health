package com.example.ui.performance

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.InfoDisclaimerCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TelemetryRow
import com.example.ui.components.TelemetrySection
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.RoseAlert

@Composable
fun PerformanceScreen(
    viewModel: PerformanceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mem = state.memoryInfo
    val cpu = state.cpuInfo

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
                    title = "System Performance",
                    subtitle = "RAM allocation & CPU hardware specs",
                    onRefresh = { viewModel.refresh() }
                )
            }

            // RAM Hero Card
            item {
                RamHeroCard(
                    usedGb = mem.usedGb,
                    totalGb = mem.totalGb,
                    availGb = mem.availableGb,
                    usagePct = mem.usagePercentage,
                    isLowMem = mem.isLowMemory
                )
            }

            // RAM Details
            item {
                TelemetrySection(
                    title = "System Memory (RAM) Allocation",
                    icon = Icons.Default.Memory
                ) {
                    val totalGb = String.format("%.2f GB", mem.totalGb)
                    val usedGb = String.format("%.2f GB", mem.usedGb)
                    val availGb = String.format("%.2f GB", mem.availableGb)
                    val threshMb = String.format("%.0f MB", mem.thresholdMb)

                    TelemetryRow(label = "Total System RAM", value = totalGb)
                    TelemetryRow(label = "Used Memory (Apps & Cache)", value = "$usedGb (${mem.usagePercentage}%)")
                    TelemetryRow(label = "Available Free RAM", value = availGb)
                    TelemetryRow(label = "Kernel Low Memory State", value = if (mem.isLowMemory) "ACTIVE (Low Memory Warning)" else "Normal")
                    TelemetryRow(label = "Low Memory Threshold", value = threshMb, showDivider = false)
                }
            }

            // Android Memory Architecture Explanation
            item {
                InfoDisclaimerCard(
                    text = "Android memory architecture uses unused RAM for background process caching and Linux buffer caches to ensure fast app resumption. A high RAM percentage is normal, expected, and beneficial for performance."
                )
            }

            // CPU Hardware Section
            item {
                TelemetrySection(
                    title = "CPU & Instruction Sets",
                    icon = Icons.Default.Speed
                ) {
                    TelemetryRow(label = "Processor Core Count", value = "${cpu.coreCount} Cores")
                    TelemetryRow(label = "Primary ABI", value = cpu.primaryAbi)
                    TelemetryRow(label = "Supported ABIs", value = cpu.supportedAbis.joinToString(", "))
                    TelemetryRow(label = "64-bit ABIs", value = if (cpu.supported64BitAbis.isNotEmpty()) cpu.supported64BitAbis.joinToString(", ") else "None")
                    TelemetryRow(label = "32-bit ABIs", value = if (cpu.supported32BitAbis.isNotEmpty()) cpu.supported32BitAbis.joinToString(", ") else "None")
                    TelemetryRow(label = "Hardware Chipset Code", value = cpu.hardwareName)
                    TelemetryRow(label = "Board Code", value = cpu.boardName, showDivider = false)
                }
            }

            // Uptime & System Timers Section
            item {
                TelemetrySection(
                    title = "System Timers & Uptime",
                    icon = Icons.Default.Schedule
                ) {
                    TelemetryRow(label = "Total Device Uptime", value = cpu.formattedElapsedUptime)
                    TelemetryRow(label = "Awake Uptime (Active)", value = cpu.formattedAwakeUptime)
                    TelemetryRow(label = "Deep Sleep Time", value = cpu.formattedDeepSleepUptime, showDivider = false)
                }
            }

            // CPU Security Sandbox Notice
            item {
                InfoDisclaimerCard(
                    text = "Modern Android security policies restrict non-root access to raw CPU clock frequency files (/sys/devices/system/cpu). Phone Health respects platform privacy and reports genuine system-level metrics without estimating."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RamHeroCard(
    usedGb: Double,
    totalGb: Double,
    availGb: Double,
    usagePct: Int,
    isLowMem: Boolean,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        isLowMem -> RoseAlert
        usagePct < 75 -> EmeraldTeal
        usagePct < 88 -> CyanAccent
        else -> AmberWarning
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ram_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(progressColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SYSTEM MEMORY USAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.1.sp
                        )
                        Text(
                            text = "${String.format("%.1f", usedGb)} GB of ${String.format("%.1f", totalGb)} GB used",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "$usagePct%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = (usagePct / 100f).coerceIn(0f, 1f),
                label = "ram_progress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Free Headroom: ${String.format("%.2f", availGb)} GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isLowMem) "Kernel Low Memory Flag" else "Kernel Memory Normal",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isLowMem) RoseAlert else EmeraldTeal
                )
            }
        }
    }
}
