package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.battery.BatteryScreen
import com.example.ui.battery.BatteryViewModel
import com.example.ui.hardware.HardwareScreen
import com.example.ui.hardware.HardwareViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.network.NetworkScreen
import com.example.ui.network.NetworkViewModel
import com.example.ui.performance.PerformanceScreen
import com.example.ui.performance.PerformanceViewModel
import com.example.ui.settings.PrivacyPolicyScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.storage.StorageScreen
import com.example.ui.storage.StorageViewModel

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel = viewModel(),
    batteryViewModel: BatteryViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel(),
    hardwareViewModel: HardwareViewModel = viewModel(),
    networkViewModel: NetworkViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    storageViewModel: StorageViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if the current route is one of the bottom navigation items
    val isBottomBarVisible = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        val icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToBattery = { navController.navigate(Screen.Battery.route) },
                    onNavigateToPerformance = { navController.navigate(Screen.Performance.route) },
                    onNavigateToStorage = { navController.navigate(Screen.Storage.route) },
                    onNavigateToHardware = { navController.navigate(Screen.Hardware.route) },
                    onNavigateToNetwork = { navController.navigate(Screen.Network.route) }
                )
            }

            composable(Screen.Battery.route) {
                BatteryScreen(viewModel = batteryViewModel)
            }

            composable(Screen.Performance.route) {
                PerformanceScreen(viewModel = performanceViewModel)
            }

            composable(Screen.Hardware.route) {
                HardwareScreen(viewModel = hardwareViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            composable(Screen.Storage.route) {
                StorageScreen(viewModel = storageViewModel)
            }

            composable(Screen.Network.route) {
                NetworkScreen(viewModel = networkViewModel)
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
