package com.ahao.wifichat.net

import com.ahao.wifichat.event.NetworkConnectionListener
import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.net.socket.UDPReceiver
import com.ahao.wifichat.net.socket.UDPSender
import com.ahao.wifichat.entity.Userinfo
import java.util.logging.Logger

/** 网络服务门面：统一管理连接状态、UDP 收发 */
class NetworkService(userinfo: Userinfo, errorHandler: ErrorHandler) : NetworkConnectionListener {

    private val connectionWorker = ConnectionWorker(userinfo)
    private val udpSender = UDPSender(errorHandler)
    private val udpReceiver = UDPReceiver(userinfo, errorHandler)

    init {
        LOG.fine("Initializing network")
        connectionWorker.registerNetworkConnectionListener(this)
    }

    fun connect() = connectionWorker.start()

    fun disconnect() = connectionWorker.stop()

    val isConnectionWorkerAlive: Boolean get() = connectionWorker.isAlive

    val isNetworkUp: Boolean get() = connectionWorker.isNetworkUp

    fun registerNetworkConnectionListener(listener: NetworkConnectionListener) {
        connectionWorker.registerNetworkConnectionListener(listener)
    }

    fun registerUDPReceiverListener(listener: ReceiverListener) {
        udpReceiver.registerReceiverListener(listener)
    }

    fun sendUDPMsg(message: String, ip: String?, port: Int): Boolean = udpSender.send(message, ip, port)

    fun checkNetwork() = connectionWorker.checkNetwork()

    override fun networkWentDown(silent: Boolean) {
        udpSender.stopSender()
        udpReceiver.stopReceiver()
    }

    override fun beforeNetworkCameUp() {
        // 无需准备
    }

    override fun networkCameUp(silent: Boolean) {
        udpSender.startSender()
        udpReceiver.startReceiver()
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(NetworkService::class.java.name)
    }
}
