package com.sanvar.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class SmartFilePrinter(
    private val logDir: String,
    private val saveDay: Int = 2,
    private val prefix: String = "KLog",
) : LogPrinter {

    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val fileFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // 使用 UNLIMITED 保证非阻塞发送
    private val logChannel = Channel<String>(Channel.Factory.UNLIMITED)

    // 共享状态
    private val isReleased = AtomicBoolean(false)


    // 文件相关
    private var currentFileDate: String = fileFormat.format(Date())
    private var writer: BufferedWriter? = null

    init {
        initFileWriter()
        startLogProcessor()
        startMaintenanceTask()
        startFlushTimer()
    }

    private fun startFlushTimer() {
        logScope.launch {
            while (isActive && !isReleased.get()) {
                // BufferedWriter 不支持查询缓冲区状态，因此采用固定间隔刷新的稳健策略。
                // 这在确保日志及时写入和避免过于频繁的IO操作之间取得了良好平衡。
                delay(3000)
                try {
                    writer?.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 启动日志处理协程
    private fun startLogProcessor() {
        logScope.launch {
            // 批量处理缓冲区
            for (message in logChannel) {
                if (!isActive || isReleased.get()) break
                try {
                    writer?.write(message)
                } catch (e: Exception) {
                    try {
                        writer?.flush()
                    } catch (e: Exception) {
                        // 忽略次要异常
                    }
                }
            }
        }
    }

    // 启动文件维护协程
    private fun startMaintenanceTask() {
        logScope.launch {
            while (isActive && !isReleased.get()) {
                checkDateAndRotate()
                deleteExpiredFiles()

                // 仅每分钟执行一次
                delay(60_000L) // 1 minute
            }
        }
    }

    private fun initFileWriter() {
        // 在启动时和文件轮换时调用
        try {
            writer?.flush()
            writer?.close()
            val dir = File(logDir).apply { mkdirs() }
            val newFile = File(dir, "${prefix}_$currentFileDate.txt")
            writer = BufferedWriter(FileWriter(newFile, true))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkDateAndRotate() {
        val currentDate = fileFormat.format(Date())
        if (currentDate != currentFileDate) {
            currentFileDate = currentDate
            initFileWriter()
        }
    }

    private fun deleteExpiredFiles() {
        try {
            val cutoff = System.currentTimeMillis() - saveDay * 24 * 60 * 60 * 1000L
            File(logDir).listFiles()?.forEach { file ->
                if (file.name.startsWith(prefix) && file.isFile) {
                    if (file.lastModified() < cutoff) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun printer(priority: Int, message: String) {
        if (isReleased.get()) return
        val logLine = "${timeFormat.format(Date())}| $message\n"
        logChannel.trySend(logLine)
    }

    fun release() {
        isReleased.set(true)
        logScope.cancel()
        try {
            writer?.flush()
            writer?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
