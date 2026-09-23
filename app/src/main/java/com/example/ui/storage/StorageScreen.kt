package com.example.ui.storage

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.storage.StoragePartition
import com.example.ui.components.InfoDisclaimerCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TelemetryRow
import com.example.ui.components.TelemetrySection
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.RoseAlert

@Composable
fun StorageScreen(
    viewModel: StorageViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val internal = state.storageInfo.internalStorage
    val external = state.storageInfo.externalStorage

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
                    title = "Storage Telemetry",
                    subtitle = "Internal flash & partition space analysis",
                    onRefresh = { viewModel.refresh() }
                )
            }

            // Circular Storage Gauge Card
            item {
                StorageHeroGaugeCard(partition = internal)
            }

            // Internal Storage Breakdown
            item {
                TelemetrySection(
                    title = "Internal Storage Partition",
                    icon = Icons.Default.Storage
                ) {
                    val totalGb = String.format("%.2f GB", internal.totalGb)
                    val usedGb = String.format("%.2f GB", internal.usedGb)
                    val freeGb = String.format("%.2f GB", internal.freeGb)
                    val availGb = String.format("%.2f GB", internal.availableGb)

                    TelemetryRow(label = "Mount Path", value = internal.path)
                    TelemetryRow(label = "Total Capacity", value = totalGb)
                    TelemetryRow(label = "Used Space", value = "$usedGb (${internal.usagePercentage}%)")
                    TelemetryRow(label = "Free Physical Space", value = freeGb)
                    TelemetryRow(label = "Available for Apps", value = availGb, showDivider = false)
                }
            }

            // External / Shared Storage Breakdown if detected
            if (external != null) {
                item {
                    TelemetrySection(
                        title = external.name,
                        icon = Icons.Default.SdCard
                    ) {
                        val totalGb = String.format("%.2f GB", external.totalGb)
                        val usedGb = String.format("%.2f GB", external.usedGb)
                        val freeGb = String.format("%.2f GB", external.freeGb)

                        TelemetryRow(label = "Mount Path", value = external.path)
                        TelemetryRow(label = "Total Space", value = totalGb)
                        TelemetryRow(label = "Used Space", value = "$usedGb (${external.usagePercentage}%)")
                        TelemetryRow(label = "Free Space", value = freeGb)
                        TelemetryRow(label = "Removable Media", value = if (external.isRemovable) "Yes" else "No", showDivider = false)
                    }
                }
            }

            // Educational note about OS file system space
            item {
                InfoDisclaimerCard(
                    text = "Storage metrics are queried directly from Android's StatFs system API for mounted partitions. Total capacity reflects formatted filesystem space after filesystem block reservations."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StorageHeroGaugeCard(
    partition: StoragePartition,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    val target = (partition.usagePercentage / 100f).coerceIn(0f, 1f)

    LaunchedEffect(target) {
        animatedProgress.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val arcColor = when {
        partition.usagePercentage < 75 -> CyanAccent
        partition.usagePercentage < 90 -> AmberWarning
        else -> RoseAlert
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIMARY STORAGE USAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "${String.format("%.1f", partition.usedGb)} GB of ${String.format("%.1f", partition.totalGb)} GB used",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(arcColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", partition.freeGb)} GB Free",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = arcColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = 14.dp.toPx()
                    drawCircle(color = trackColor, style = Stroke(stroke))
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress.value * 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress.value * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 38.sp
                    )
                    Text(
                        text = "Used Space",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
