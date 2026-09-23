package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.DeviceHealthSummary
import com.example.domain.model.HardwareTelemetryStatus
import com.example.domain.model.SystemConditionLevel
import com.example.ui.components.HealthGaugeCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleSummary = DeviceHealthSummary(
        conditionLevel = SystemConditionLevel.OPERATIONAL,
        conditionLabel = "All Systems Operational",
        conditionDescription = "Real-time Android OS telemetry verified",
        batteryStatus = HardwareTelemetryStatus("Battery Condition", "Good", "Reported by OS battery driver", true),
        thermalStatus = HardwareTelemetryStatus("Thermal State", "Normal", "PowerManager thermal status", true),
        memoryStatus = HardwareTelemetryStatus("Kernel Memory", "Normal", "3.2 GB available RAM", true),
        storageStatus = HardwareTelemetryStatus("Storage Headroom", "58.4 GB Free", "54% storage headroom", true),
        isAvailable = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        HealthGaugeCard(summary = sampleSummary)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
