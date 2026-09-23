package com.example.ui.hardware

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HardwareViewModelTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `viewModel can be instantiated by AndroidViewModelFactory reflection`() {
        val constructor = HardwareViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
        val viewModel = constructor.newInstance(application)
        assertNotNull(viewModel)
    }

    @Test
    fun `viewModel exposes buildDetails flow with manufacturer model and androidVersion`() = runTest {
        val viewModel = HardwareViewModel(application)

        val build = viewModel.buildDetails.first()
        val manufacturer = viewModel.manufacturer.first()
        val model = viewModel.model.first()
        val androidVersion = viewModel.androidVersion.first()

        assertNotNull(build)
        assertEquals(Build.MANUFACTURER.replaceFirstChar { it.uppercase() }, manufacturer)
        assertEquals(Build.MODEL, model)
        assertEquals(Build.VERSION.RELEASE, androidVersion)
    }

    @Test
    fun `viewModel uiState contains complete hardware and build telemetry`() {
        val viewModel = HardwareViewModel(application)
        val state = viewModel.uiState.value

        assertNotNull(state.buildDetails)
        assertNotNull(state.deviceInfo)
        assertNotNull(state.hardwareInfo)
        assertEquals(Build.MODEL, state.buildDetails.model)
        assertEquals(Build.VERSION.RELEASE, state.buildDetails.androidVersion)
    }

    @Test
    fun `viewModel refresh updates state`() {
        val viewModel = HardwareViewModel(application)
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertNotNull(state.buildDetails)
        assertTrue(state.buildDetails.manufacturer.isNotBlank())
    }
}
