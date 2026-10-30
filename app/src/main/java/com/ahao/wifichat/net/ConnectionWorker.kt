package com.ahao.wifichat.net

import com.ahao.wifichat.entity.Userinfo
import com.ahao.wifichat.event.NetworkConnectionListener
import com.ahao.wifichat.utlis.NetworkUtils
import java.net.NetworkInterface
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Logger

/**
 * 网络监测线程：周期性检查可用网卡，在网络上线/下线/切换时通知监听器。
 */
class ConnectionWorker(private val userinfo: Userinfo) : Runnable {

    private val listeners = CopyOnWriteArrayList<NetworkConnectionListener>()

    @Volatile
    private var run = false

    @Volatile
    var isNetworkUp = false
        private set

    private var networkInterface: NetworkInterface? = null
    private var worker: Thread? = null

    /** 当前网卡（若已失效则返回最新的同名网卡） */
    val currentNetworkInterface: NetworkInterface?
        get() = NetworkUtils.getUpdatedNetworkInterface(networkInterface) ?: networkInterface

    val isAlive: Boolean get() = worker?.isAlive ?: false

    override fun run() {
        LOG.fine("Network is starting")
        while (run) {
            val networkUp = updateNetwork()
            if (!run) break
            try {
                Thread.sleep(if (networkUp) SLEEP_UP else SLEEP_DOWN)
            } catch (e: InterruptedException) {
                // 被 stop() 或 checkNetwork() 中断
                LOG.fine(e.toString())
            }
        }
        LOG.fine("Network is stopping")
        if (isNetworkUp) notifyNetworkDown(false)
        networkInterface = null
    }

    fun checkNetwork() {
        worker?.interrupt()
    }

    @Synchronized
    private fun updateNetwork(): Boolean {
        val netif = selectNetworkInterface()

        when {
            // 没有可用网卡
            !NetworkUtils.isUsable(netif) -> {
                LOG.fine("Network is down")
                if (isNetworkUp) notifyNetworkDown(false)
                return false
            }
            // 切换网卡，例如从有线切换到无线
            !NetworkUtils.sameNetworkInterface(netif, networkInterface) -> {
                LOG.fine("Changing network from ${networkInterface?.name ?: "[null]"} to ${netif?.name}")
                networkInterface = netif
                if (isNetworkUp) {
                    notifyNetworkDown(true)
                    notifyNetworkUp(true)
                } else {
                    notifyNetworkUp(false)
                }
            }
            // 同一网卡重新恢复连接，例如重新插上网线
            !isNetworkUp -> {
                LOG.fine("Network ${netif?.name} is up again")
                networkInterface = netif
                notifyNetworkUp(false)
            }
        }
        // 其它情况：原连接仍然有效
        return true
    }

    @Synchronized
    private fun notifyNetworkUp(silent: Boolean) {
        listeners.forEach { it.beforeNetworkCameUp() }
        isNetworkUp = true
        listeners.forEach { it.networkCameUp(silent) }
    }

    @Synchronized
    private fun notifyNetworkDown(silent: Boolean) {
        isNetworkUp = false
        listeners.forEach { it.networkWentDown(silent) }
    }

    fun registerNetworkConnectionListener(listener: NetworkConnectionListener) {
        listeners += listener
    }

    @Synchronized
    fun start() {
        if (!run && !isAlive) {
            run = true
            worker = Thread(this, "ConnectionWorker").also { it.start() }
        }
    }

    fun stop() {
        run = false
        worker?.interrupt()
    }

    /** 优先使用配置中保存的网卡，否则使用第一个可用网卡 */
    private fun selectNetworkInterface(): NetworkInterface? {
        val firstUsable = NetworkUtils.findFirstUsableNetworkInterface()
        if (firstUsable == null) {
            LOG.finer("No usable network interface detected.")
            return null
        }

        val saved = NetworkUtils.getNetworkInterfaceByName(userinfo.ipAdderss)
        if (NetworkUtils.isUsable(saved)) {
            LOG.finer("Using saved network interface: \n${NetworkUtils.getNetworkInterfaceInfo(saved)}")
            return saved
        }

        LOG.finer("Saved network interface '${userinfo.ipAdderss}' is invalid: \n${NetworkUtils.getNetworkInterfaceInfo(saved)}")
        return firstUsable
    }

    private companion object {
        val LOG: Logger = Logger.getLogger(ConnectionWorker::class.java.name)
        const val SLEEP_UP = 1000 * 60L
        const val SLEEP_DOWN = 1000 * 15L
    }
}
