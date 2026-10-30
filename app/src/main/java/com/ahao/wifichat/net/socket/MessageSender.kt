package com.ahao.wifichat.net.socket

import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.net.Constants
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

/** 组播发送 */
class MessageSender(
    ipAddress: String = Constants.NETWORK_TEMP_IP,
    private val port: Int = Constants.NETWORK_TEMP_PORT,
    errorHandler: ErrorHandler,
) {
    private var mcSocket: MulticastSocket? = null
    private val address: InetAddress

    @Volatile
    private var connected = false

    init {
        LOG.fine("Creating MessageSender on $ipAddress:$port")
        require(ipAddress.isNotBlank()) { "IP address can not be empty" }
        address = try {
            InetAddress.getByName(ipAddress)
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, e.toString(), e)
            errorHandler.showCriticalError("Failed to initialize the network:\n$e\n${Constants.APP_NAME} will now shutdown.")
            exitProcess(1)
        }
    }

    @Synchronized
    fun send(message: String): Boolean {
        val socket = mcSocket
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
            socket.send(DatagramPacket(encodedMsg, size, address, port))
            LOG.fine("Sent message: $message")
            true
        } catch (e: IOException) {
            LOG.log(Level.WARNING, "Could not send message: $message", e)
            false
        }
    }

    @Synchronized
    fun startSender(networkInterface: NetworkInterface?): Boolean {
        LOG.fine("Connecting to ${address.hostAddress}:$port on $networkInterface")
        try {
            if (connected) {
                LOG.fine("Already connected.")
            } else {
                val socket = mcSocket ?: MulticastSocket(port).also { mcSocket = it }
                if (networkInterface != null) socket.networkInterface = networkInterface
                socket.joinGroup(address)
                socket.timeToLive = 64
                LOG.fine("Connected to ${socket.networkInterface}")
                connected = true
            }
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, "Could not start sender: $e", e)
            mcSocket?.let { if (!it.isClosed) it.close() }
            mcSocket = null
        }
        return connected
    }

    @Synchronized
    fun stopSender() {
        LOG.fine("Disconnecting from ${address.hostAddress}:$port")
        if (!connected) {
            LOG.fine("Not connected.")
            return
        }
        connected = false
        val socket = mcSocket ?: return
        try {
            if (!socket.isClosed) socket.leaveGroup(address)
        } catch (e: IOException) {
            LOG.log(Level.WARNING, e.toString())
        }
        if (!socket.isClosed) {
            socket.close()
            mcSocket = null
        }
        LOG.fine("Disconnected from ${address.hostAddress}:$port")
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(MessageSender::class.java.name)
    }
}
