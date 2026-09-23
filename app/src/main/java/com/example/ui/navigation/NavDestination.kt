package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Home : Screen(
        route = "home",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    object Battery : Screen(
        route = "battery",
        title = "Battery",
        selectedIcon = Icons.Filled.BatteryChargingFull,
        unselectedIcon = Icons.Outlined.BatteryStd
    )

    object Performance : Screen(
        route = "performance",
        title = "Performance",
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed
    )

    object Hardware : Screen(
        route = "hardware",
        title = "Hardware",
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Sub-screens
    object Storage : Screen(route = "storage", title = "Storage Details")
    object Network : Screen(route = "network", title = "Network Telemetry")
    object PrivacyPolicy : Screen(route = "privacy_policy", title = "Privacy Policy")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Battery,
    Screen.Performance,
    Screen.Hardware,
    Screen.Settings
)
