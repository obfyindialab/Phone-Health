package com.example.ui.network

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnLock
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.ConnectionType
import com.example.ui.components.InfoDisclaimerCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.TelemetryRow
import com.example.ui.components.TelemetrySection
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.RoseAlert

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val net = state.networkInfo

    val activeColor = if (net.isConnected) EmeraldTeal else RoseAlert
    val netIcon = when (net.connectionType) {
        ConnectionType.WIFI -> Icons.Default.Wifi
        ConnectionType.CELLULAR -> Icons.Default.CellTower
        ConnectionType.VPN -> Icons.Default.VpnLock
        else -> if (net.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff
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
                    title = "Network Telemetry",
                    subtitle = "Real-time connectivity & transport state"
                )
            }

            // Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("network_hero_card"),
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
                                text = "ACTIVE INTERNET LINK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (net.isConnected) net.connectionType.displayName else "Disconnected",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (net.isInternetValidated) "Internet Route Validated by OS" else if (net.isConnected) "Connected (Captive/Unvalidated)" else "No active route to the internet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(activeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = netIcon,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            // Network Capabilities Breakdown
            item {
                TelemetrySection(
                    title = "Active Link Diagnostics",
                    icon = Icons.Default.CloudDone
                ) {
                    TelemetryRow(label = "Primary Transport", value = net.connectionType.displayName)
                    TelemetryRow(label = "Network Online Status", value = if (net.isConnected) "Connected" else "Offline")
                    TelemetryRow(label = "Internet Reachability Validated", value = if (net.isInternetValidated) "Yes (Validated)" else "No")
                    TelemetryRow(label = "Data Metering Flag", value = if (net.isMetered) "Metered Connection" else "Unmetered (Unlimited)")
                    TelemetryRow(label = "Downstream Link Estimate", value = net.downstreamSpeedFormatted)
                    TelemetryRow(label = "Upstream Link Estimate", value = net.upstreamSpeedFormatted, showDivider = false)
                }
            }

            // Privacy Assurance Note
            item {
                InfoDisclaimerCard(
                    text = "Privacy Guarantee: Phone Health strictly checks high-level network capabilities through Android's ConnectivityManager. The app never scans, logs, collects, or transmits your Wi-Fi SSID, BSSID, IP addresses, router MAC, or browser traffic."
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
