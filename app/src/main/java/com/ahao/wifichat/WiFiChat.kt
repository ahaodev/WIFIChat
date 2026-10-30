package com.ahao.wifichat

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.ahao.wifichat.entity.Userinfo
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.net.file.TcpService
import com.ahao.wifichat.service.P2PService
import com.ahao.wifichat.utlis.NetworkUtils
import java.io.File
import kotlin.system.exitProcess

/** 应用级上下文：管理后台服务连接、文件接收服务与当前用户 */
class WiFiChat(base: Context) : ContextWrapper(base) {

    /** 与 [P2PService] 的绑定对象，连接建立后才可用 */
    var binder: P2PService.P2PServiceBindle? = null
        private set

    val activityManager = ActivityManager()

    /** 当前登录用户 */
    val me = Userinfo(ipAdderss = NetworkUtils.getLocalIpAddress(), port = Constants.NETWORK_UDP_PORT)

    private val intentService = Intent(this, P2PService::class.java)

    private var tcpService: TcpService? = null

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as P2PService.P2PServiceBindle
            initTCPService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // 服务意外断开时暂不处理
        }
    }

    init {
        startService()
    }

    fun startService() {
        startService(intentService)
        bindService(intentService, conn, BIND_AUTO_CREATE)
    }

    fun stopService() {
        stopService(intentService)
        unbindService(conn)
    }

    fun exit() {
        unbindService(conn)
        exitProcess(0)
    }

    /** 启动文件接收线程，保存到应用目录 */
    fun initTCPService() {
        val savedir = File(WifiChatApplication.instance.appPath)
        if (!savedir.exists()) savedir.mkdirs()
        tcpService = TcpService.getInstance(this).also {
            TcpService.setHandler(Handler(Looper.getMainLooper()))
            it.setSavePath(savedir.absolutePath)
            it.startReceive()
        }
    }

    companion object {
        lateinit var instance: WiFiChat
            private set

        fun init(app: Application) {
            synchronized(WiFiChat::class.java) {
                instance = WiFiChat(app)
            }
        }
    }
}
