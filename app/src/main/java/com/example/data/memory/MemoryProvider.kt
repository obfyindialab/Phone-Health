package com.example.data.memory

import android.app.ActivityManager
import android.content.Context

class MemoryProvider(private val context: Context) {

    private val activityManager: ActivityManager? by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    }

    fun getMemoryInfo(): MemoryInfo {
        val am = activityManager ?: return fallbackMemoryInfo()
        return try {
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)

            val total = memInfo.totalMem
            val available = memInfo.availMem
            val used = (total - available).coerceAtLeast(0L)
            val usagePct = if (total > 0L) {
                ((used.toDouble() / total.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0

            MemoryInfo(
                totalBytes = total,
                availableBytes = available,
                usedBytes = used,
                usagePercentage = usagePct,
                isLowMemory = memInfo.lowMemory,
                thresholdBytes = memInfo.threshold
            )
        } catch (_: Exception) {
            fallbackMemoryInfo()
        }
    }

    private fun fallbackMemoryInfo(): MemoryInfo {
        return MemoryInfo(
            totalBytes = 0L,
            availableBytes = 0L,
            usedBytes = 0L,
            usagePercentage = 0,
            isLowMemory = false,
            thresholdBytes = 0L
        )
    }
}
