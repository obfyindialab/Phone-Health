package com.example.data.storage

data class StoragePartition(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val availableBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usagePercentage: Int,
    val isRemovable: Boolean = false
) {
    val totalGb: Double get() = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val usedGb: Double get() = usedBytes / (1024.0 * 1024.0 * 1024.0)
    val availableGb: Double get() = availableBytes / (1024.0 * 1024.0 * 1024.0)
    val freeGb: Double get() = freeBytes / (1024.0 * 1024.0 * 1024.0)
}

data class StorageInfo(
    val internalStorage: StoragePartition,
    val externalStorage: StoragePartition?
)
