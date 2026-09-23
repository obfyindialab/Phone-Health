package com.example.data.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File

class StorageProvider(private val context: Context) {

    fun getStorageInfo(): StorageInfo {
        val internalPartition = getPartitionInfo(
            name = "Internal Storage",
            path = Environment.getDataDirectory().path,
            isRemovable = false
        ) ?: fallbackPartition("Internal Storage", "/data")

        val externalPartition = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val extFile = Environment.getExternalStorageDirectory()
            if (extFile != null && extFile.canRead()) {
                val isRemovable = Environment.isExternalStorageRemovable()
                getPartitionInfo(
                    name = if (isRemovable) "SD Card / External" else "Shared Storage",
                    path = extFile.path,
                    isRemovable = isRemovable
                )
            } else null
        } else null

        return StorageInfo(
            internalStorage = internalPartition,
            externalStorage = externalPartition
        )
    }

    private fun getPartitionInfo(name: String, path: String, isRemovable: Boolean): StoragePartition? {
        return try {
            val stat = StatFs(path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            val freeBlocks = stat.freeBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val freeBytes = freeBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

            val usagePercentage = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0

            StoragePartition(
                name = name,
                path = path,
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usagePercentage = usagePercentage,
                isRemovable = isRemovable
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun fallbackPartition(name: String, path: String): StoragePartition {
        return StoragePartition(
            name = name,
            path = path,
            totalBytes = 0L,
            availableBytes = 0L,
            freeBytes = 0L,
            usedBytes = 0L,
            usagePercentage = 0,
            isRemovable = false
        )
    }
}
