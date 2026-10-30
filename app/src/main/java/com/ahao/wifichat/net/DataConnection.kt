package com.ahao.wifichat.net

import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.misc.ErrorHandler
import com.ahao.wifichat.protocol.codec.Codec
import com.ahao.wifichat.protocol.codec.Pack
import com.ahao.wifichat.service.P2PService
import com.ahao.wifichat.service.PacketListener
import com.ahao.wifichat.service.ProcessReceiverData
import com.ahao.wifichat.service.filter.PacketFilter
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor

/** 数据连接：负责编码发送、接收分发，以及网络的连接与断开 */
class DataConnection(val service: P2PService) : ReceiverListener {

    private val recvListeners = ConcurrentHashMap<PacketListener, ListenerWrapper>()
    private val notifier = ScheduledThreadPoolExecutor(1)
    private val sendExecutor: ExecutorService = Executors.newFixedThreadPool(5)

    private val selfUser = WiFiChat.instance.me
    private val errorHandler = ErrorHandler()
    private val networkService = NetworkService(selfUser, errorHandler)
    private val processReceiverData = ProcessReceiverData(this)

    init {
        networkService.registerUDPReceiverListener(this)
        connect()
    }

    fun connect() {
        if (!networkService.isConnectionWorkerAlive) networkService.connect()
    }

    fun disconnect() = networkService.disconnect()

    fun onDestroy() {
        sendExecutor.shutdownNow()
        notifier.shutdownNow()
        disconnect()
    }

    /** 编码后通过 UDP 发送给 [destUser] */
    @Synchronized
    fun sendData(destUser: OnlineUserInfo, pack: Pack): Boolean =
        submit(SendDataUDP(networkService, destUser, Codec.encode(pack)))

    /** 广播发送 */
    fun sendData(pack: Pack): Boolean =
        sendData(
            OnlineUserInfo(ipAdderss = Constants.NETWORK_BROADCAST_IP, port = Constants.NETWORK_UDP_PORT),
            pack,
        )

    fun submit(call: Callable<Boolean>): Boolean {
        if (sendExecutor.isShutdown) return false
        return sendExecutor.submit(call).isDone
    }

    @Synchronized
    override fun messageArrived(message: String, ipAddress: String, port: Int) {
        // 过滤自己发送的消息
        if (selfUser.ipAdderss == ipAddress) return
        val pack = runCatching { Codec.decode(message) }.getOrNull() ?: return
        processPacket(pack)
    }

    /** 将数据包交给所有匹配过滤器的监听器（在后台线程执行） */
    fun processPacket(pack: Pack) {
        notifier.submit(Runnable {
            recvListeners.values.forEach { it.notifyListener(pack) }
        })
    }

    /** 注册数据包监听器，仅接收满足 [packetFilter] 的数据包 */
    fun addPacketListener(packetFilter: PacketFilter, packetListener: PacketListener) {
        recvListeners[packetListener] = ListenerWrapper(packetFilter, packetListener)
    }

    fun removePacketListener(packetListener: PacketListener): Boolean = recvListeners.remove(packetListener) != null

    private class ListenerWrapper(
        private val packetFilter: PacketFilter?,
        private val packetListener: PacketListener,
    ) {
        fun notifyListener(pack: Pack) {
            if (packetFilter?.accept(pack) == true) packetListener.processPacket(pack)
        }
    }
}
