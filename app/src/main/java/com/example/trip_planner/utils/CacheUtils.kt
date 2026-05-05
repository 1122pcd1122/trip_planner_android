package com.example.trip_planner.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 缓存管理工具类
 */
object CacheUtils {

    /**
     * 获取应用缓存大小（字节）
     */
    fun getCacheSize(context: Context): Long {
        var size = 0L
        val cacheDir = context.cacheDir
        if (cacheDir.exists()) {
            size += getDirSize(cacheDir)
        }
        val externalCacheDir = context.externalCacheDir
        if (externalCacheDir != null && externalCacheDir.exists()) {
            size += getDirSize(externalCacheDir)
        }
        return size
    }

    private fun getDirSize(dir: java.io.File): Long {
        var size = 0L
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    getDirSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    /**
     * 格式化缓存大小
     */
    fun formatCacheSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.2f MB", size / (1024.0 * 1024.0))
        }
    }

    /**
     * 清除应用缓存
     */
    suspend fun clearAppCache(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            deleteDir(context.cacheDir)
            context.externalCacheDir?.let { deleteDir(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deleteDir(dir: java.io.File): Boolean {
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDir(file)
                    } else {
                        file.delete()
                    }
                }
            }
            return dir.delete()
        }
        return true
    }
}
