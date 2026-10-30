package com.ahao.wifichat.net.socket

import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.net.Constants
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.logging.Level
import java.util.logging.Logger

/** UDP 单播发送 */
class UDPSender(private val errorHandler: ErrorHandler) {

    private var udpSocket: DatagramSocket? = null

    @Volatile
    private var connected = false

    fun send(message: String, ip: String?, port: Int): Boolean {
        val socket = udpSocket
        if (!connected || socket == null) return false

        return try {
            val encodedMsg = message.toByteArray(charset(Constants.MESSAGE_CHARSET))
            val size = encodedMsg.size
            if (size > Constants.NETWORK_PACKET_SIZE) {
                LOG.log(
                    Level.WARNING,
                    "Message was $size bytes, which is too large.\n The receiver might not get the complete message.\n'$message'",
                )
            }
            socket.send(DatagramPacket(encodedMsg, size, InetAddress.getByName(ip), port))
            LOG.fine { "Sent message: $message to $ip:$port" }
            true
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, "Could not send message: $message", e)
            false
        }
    }

    @Synchronized
    fun startSender() {
        LOG.fine("Connecting...")
        if (connected) {
            LOG.fine("Already connected.")
            return
        }
        try {
            udpSocket = DatagramSocket()
            connected = true
            LOG.fine("Connected.")
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, e.toString(), e)
            errorHandler.showError("Failed to initialize network:\n$e\n\nYou will not be able to send private messages!")
        }
    }

    @Synchronized
    fun stopSender() {
        LOG.fine("Disconnecting...")
        if (!connected) {
            LOG.fine("Not connected.")
            return
        }
        connected = false
        udpSocket?.takeIf { !it.isClosed }?.close()
        LOG.fine("Disconnected.")
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(UDPSender::class.java.name)
    }
}
