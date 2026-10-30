package com.ahao.wifichat.net.socket

import com.ahao.wifichat.entity.Userinfo
import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.net.ReceiverListener
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.logging.Level
import java.util.logging.Logger

/** UDP 单播接收：在后台线程循环读取数据包并回调 [listener] */
class UDPReceiver(
    private val userinfo: Userinfo,
    private val errorHandler: ErrorHandler,
) : Runnable {

    private var udpSocket: DatagramSocket? = null
    private var listener: ReceiverListener? = null

    @Volatile
    private var connected = false

    override fun run() {
        val socket = udpSocket ?: return
        while (connected) {
            try {
                val packet = DatagramPacket(ByteArray(Constants.NETWORK_PACKET_SIZE), Constants.NETWORK_PACKET_SIZE)
                socket.receive(packet)
                val ip = packet.address.hostAddress.orEmpty()
                val port = packet.port
                val message = String(packet.data, 0, packet.length, charset(Constants.MESSAGE_CHARSET)).trim()
                LOG.fine { "Message arrived from $ip: $message" }
                listener?.messageArrived(message, ip, port)
            } catch (e: IOException) {
                // 套接字关闭或网络断开时会走到这里
                LOG.log(if (connected) Level.WARNING else Level.FINE, e.toString())
            }
        }
    }

    @Synchronized
    fun startReceiver() {
        LOG.fine("Connecting...")
        if (connected) {
            LOG.fine("Already connected.")
            return
        }

        var port = Constants.NETWORK_UDP_PORT
        var counter = 0
        while (counter < 50 && !connected) {
            try {
                udpSocket = DatagramSocket(port)
                connected = true
                Thread(this, "UDPReceiverWorker").start()
                userinfo.port = port
                LOG.fine("Connected to port $port")
            } catch (e: IOException) {
                LOG.log(Level.SEVERE, "$e $port")
                counter++
                port++
                userinfo.port = 0
            }
        }

        if (!connected) {
            val error = "Failed to initialize udp network:" +
                "\nNo available listening port between ${Constants.NETWORK_PRIVCHAT_PORT} and ${port - 1}." +
                "\n\nYou will not be able to receive private messages!"
            LOG.log(Level.SEVERE, error)
            errorHandler.showError(error)
        }
    }

    @Synchronized
    fun stopReceiver() {
        LOG.fine("Disconnecting...")
        if (!connected) {
            LOG.fine("Not connected.")
            return
        }
        connected = false
        udpSocket?.takeIf { !it.isClosed }?.close()
        LOG.fine("Disconnected.")
    }

    fun registerReceiverListener(listener: ReceiverListener) {
        this.listener = listener
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(UDPReceiver::class.java.name)
    }
}
