package com.example.data.cpu

import android.os.Build
import android.os.SystemClock

class CpuProvider {

    fun getCpuInfo(): CpuInfo {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val supportedAbis = Build.SUPPORTED_ABIS.toList().filter { it.isNotBlank() }
        val supported64 = Build.SUPPORTED_64_BIT_ABIS.toList().filter { it.isNotBlank() }
        val supported32 = Build.SUPPORTED_32_BIT_ABIS.toList().filter { it.isNotBlank() }
        val primaryAbi = supportedAbis.firstOrNull() ?: Build.CPU_ABI ?: "Unknown"

        val elapsed = SystemClock.elapsedRealtime()
        val awake = SystemClock.uptimeMillis()

        return CpuInfo(
            coreCount = cores,
            primaryAbi = primaryAbi,
            supportedAbis = supportedAbis,
            supported64BitAbis = supported64,
            supported32BitAbis = supported32,
            uptimeMillis = elapsed,
            awakeUptimeMillis = awake,
            hardwareName = Build.HARDWARE,
            boardName = Build.BOARD
        )
    }
}
