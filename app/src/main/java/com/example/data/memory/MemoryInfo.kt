package com.example.data.memory

data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercentage: Int,
    val isLowMemory: Boolean,
    val thresholdBytes: Long
) {
    val totalGb: Double get() = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val availableGb: Double get() = availableBytes / (1024.0 * 1024.0 * 1024.0)
    val usedGb: Double get() = usedBytes / (1024.0 * 1024.0 * 1024.0)
    val thresholdMb: Double get() = thresholdBytes / (1024.0 * 1024.0)
}
