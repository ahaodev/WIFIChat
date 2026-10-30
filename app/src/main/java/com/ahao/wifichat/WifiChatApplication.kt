package com.ahao.wifichat

import android.app.Application
import android.content.Context
import android.os.Environment
import android.view.Display
import android.view.WindowManager
import com.ahao.wifichat.net.file.FileState
import com.ahao.wifichat.db.ChatDB
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class WifiChatApplication : Application() {

    private var display: Display? = null

    override fun onCreate() {
        WiFiChat.init(this)
        ChatDB.init(this)
        super.onCreate()
        instance = this
        display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    override fun onTerminate() {
        super.onTerminate()
        WiFiChat.instance.stopService() // 停止后台服务
    }

    /** 应用文件目录：优先外部存储，其次内部数据目录 */
    val appPath: String
        get() {
            val roots = listOf(Environment.getExternalStorageDirectory(), Environment.getDataDirectory())
            val root = roots.firstOrNull { it.exists() } ?: return ""
            return File(root, "Wifi-Chat").apply { if (!exists()) mkdirs() }.absolutePath
        }

    /** 屏幕宽度的四分之一 */
    val quarterWidth: Int get() = (display?.width ?: 0) / 4

    companion object {
        lateinit var instance: WifiChatApplication
            private set

        /** 正在发送的文件状态，key 为本地路径 */
        val sendFileStates = HashMap<String, FileState>()

        /** 正在接收的文件状态，key 为本地保存路径 */
        val recieveFileStates = ConcurrentHashMap<String, FileState>()
    }
}
