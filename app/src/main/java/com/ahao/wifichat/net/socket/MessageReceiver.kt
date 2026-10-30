package com.ahao.wifichat.net.socket

import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.net.ReceiverListener
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

/** 组播接收：后台线程循环读取数据包并回调 [listener] */
class MessageReceiver(
    ipAddress: String = Constants.NETWORK_TEMP_IP,
    private val port: Int = Constants.NETWORK_TEMP_PORT,
    errorHandler: ErrorHandler,
) : Runnable {
    private var mcSocket: MulticastSocket? = null
    private val address: InetAddress
    private var listener: ReceiverListener? = null
    private var worker: Thread? = null

    @Volatile
    private var connected = false

    init {
        LOG.fine("Creating MessageReceiver on $ipAddress:$port")
        require(ipAddress.isNotBlank()) { "IP address can not be empty" }
        address = try {
            InetAddress.getByName(ipAddress)
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, e.toString(), e)
            errorHandler.showCriticalError("Failed to initialize the network:\n$e\n${Constants.APP_NAME} will now shutdown.")
            exitProcess(1)
        }
    }

    override fun run() {
        while (connected) {
            try {
                val packet = DatagramPacket(ByteArray(Constants.NETWORK_PACKET_SIZE), Constants.NETWORK_PACKET_SIZE)
                if (connected) {
                    mcSocket?.receive(packet)
                    val ip = packet.address.hostAddress.orEmpty()
                    val port = packet.port
                    val message = String(packet.data, 0, packet.length, charset(Constants.MESSAGE_CHARSET)).trim()
                    LOG.fine { "Message arrived from $ip: $message" }
                    listener?.messageArrived(message, ip, port)
                }
            } catch (e: IOException) {
                // 套接字关闭或网络断开时会走到这里
                LOG.log(if (connected) Level.WARNING else Level.FINE, e.toString())
            }
        }
    }

    @Synchronized
    fun startReceiver(networkInterface: NetworkInterface?): Boolean {
        LOG.fine("Connecting to ${address.hostAddress}:$port on $networkInterface")
        try {
            if (connected) {
                LOG.fine("Already connected.")
            } else {
                val socket = mcSocket ?: MulticastSocket(port).also { mcSocket = it }
                if (networkInterface != null) socket.networkInterface = networkInterface
                socket.joinGroup(address)
                LOG.fine("Connected to ${socket.networkInterface}")
                connected = true
            }
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, "Could not start receiver: $e", e)
            mcSocket?.let { if (!it.isClosed) it.close() }
            mcSocket = null
        }
        if (connected && (worker == null || worker?.isAlive == false)) {
            LOG.fine("Starting.")
            worker = Thread(this, "MessageReceiverWorker").also { it.start() }
        }
        return connected
    }

    @Synchronized
    fun stopReceiver() {
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

    fun registerReceiverListener(listener: ReceiverListener) {
        this.listener = listener
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(MessageReceiver::class.java.name)
    }
}
