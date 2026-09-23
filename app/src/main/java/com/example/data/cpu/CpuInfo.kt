package com.example.data.cpu

data class CpuInfo(
    val coreCount: Int,
    val primaryAbi: String,
    val supportedAbis: List<String>,
    val supported64BitAbis: List<String>,
    val supported32BitAbis: List<String>,
    val uptimeMillis: Long,
    val awakeUptimeMillis: Long,
    val hardwareName: String,
    val boardName: String
) {
    val formattedElapsedUptime: String
        get() = formatDuration(uptimeMillis)

    val formattedAwakeUptime: String
        get() = formatDuration(awakeUptimeMillis)

    val deepSleepUptimeMillis: Long
        get() = (uptimeMillis - awakeUptimeMillis).coerceAtLeast(0L)

    val formattedDeepSleepUptime: String
        get() = formatDuration(deepSleepUptimeMillis)

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val days = totalSeconds / (3600 * 24)
        val hours = (totalSeconds % (3600 * 24)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            append("${minutes}m ${seconds}s")
        }.trim()
    }
}
