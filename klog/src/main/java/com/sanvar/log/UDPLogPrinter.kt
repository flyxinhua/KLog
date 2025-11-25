package com.sanvar.log

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UDPLogPrinter(
    val port: Int,
    val host: String = "255.255.255.255",
    val deviceId: String = Build.MODEL
) : LogPrinter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: DatagramSocket? = null
    private var address: InetAddress? = null

    override fun printer(priority: Int, message: String) {

        socket?.let { sock ->
            scope.launch {
                try {
                    // --- 【核心】在日志前面拼接设备ID ---
                    val data = "[$deviceId] $message".toByteArray(Charsets.UTF_8)
                    val packet = DatagramPacket(data, data.size, address, port)
                    sock.send(packet)
                } catch (e: Exception) {
                    // ignore, do not crash the app for a logging error
                }
            }
        }

    }

      fun release() {
        try {
            socket?.close()
            socket = null
        } catch (e: Exception) {
            // ignore
        }
    }


    init {
        try {
            socket = DatagramSocket().apply { broadcast = true }
            address = InetAddress.getByName(host)
        } catch (e: Exception) {
            socket = null
        }
    }
}