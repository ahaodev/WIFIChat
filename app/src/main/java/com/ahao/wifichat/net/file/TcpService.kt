package com.ahao.wifichat.net.file

import android.content.Context
import android.os.Handler
import com.ahao.wifichat.WifiChatApplication
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.utlis.AppScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * TCP 文件接收服务（单例）。
 *
 * 监听线程阻塞在 [ServerSocket.accept] 上，没有轮询；每个连接由 [AppScope.io] 中的协程处理：
 * 先读取协议头 `文件名!大小!目录!类型`，再把数据流写入磁盘。
 * [stopReceive] 关闭监听套接字，使阻塞中的 `accept()` 立即抛出异常并退出线程。
 */
class TcpService private constructor() {

    private val running = AtomicBoolean(false)

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var savePath: String? = null

    fun setSavePath(fileSavePath: String) {
        Logger.d("设置存储路径成功,路径为$fileSavePath")
        savePath = fileSavePath
    }

    /** 开始监听；已在监听时忽略 */
    @Synchronized
    fun startReceive() {
        if (!running.compareAndSet(false, true)) return
        val server = try {
            ServerSocket(Constants.TCP_SERVER_RECEIVE_PORT)
        } catch (e: IOException) {
            running.set(false)
            Logger.d("建立监听服务器ServerSocket失败")
            e.printStackTrace()
            return
        }
        serverSocket = server
        Logger.d("建立监听服务器ServerSocket成功")
        thread(name = "TcpServiceAccept", isDaemon = true) { acceptLoop(server) }
    }

    /** 停止监听；关闭套接字会让阻塞的 accept() 退出 */
    @Synchronized
    fun stopReceive() {
        running.set(false)
        serverSocket?.let { runCatching { it.close() } }
        serverSocket = null
    }

    private fun acceptLoop(server: ServerSocket) {
        Logger.d("TCP_Service线程开启")
        while (running.get()) {
            val socket = try {
                server.accept()
            } catch (e: IOException) {
                // 套接字被关闭时直接退出；其它偶发错误则继续等待下一个连接
                if (!running.get() || server.isClosed) break
                Logger.d("客户端连接失败")
                e.printStackTrace()
                continue
            }
            Logger.d("客户端连接成功")
            val path = savePath
            AppScope.io.launch { receive(socket, path) }
        }
        Logger.d("TCP_Service线程结束")
    }

    private fun receive(socket: Socket, savePath: String?) {
        socket.use { s ->
            runCatching {
                recieveFile(DataInputStream(s.getInputStream()), savePath)
            }.onFailure {
                Logger.d("接收文件失败")
                it.printStackTrace()
            }
        }
    }

    /** 读取协议头并把数据写入 `savePath/发送者/文件名` */
    private fun recieveFile(input: DataInputStream, savePath: String?) {
        val strData = input.readUTF().split("!")
        val length = strData[1].toLong()
        Logger.d("传输文件类型:${strData[3]}")

        val fileDir = File("$savePath${File.separator}${strData[2]}").apply { if (!exists()) mkdirs() }
        val fileSavePath = File(fileDir, strData[0]).absolutePath
        Logger.d("文件存储路径:$fileSavePath")

        val fs = FileState(fileSize = length, currentSize = 0L, fileName = fileSavePath, type = 1)
        WifiChatApplication.recieveFileStates[fileSavePath] = fs

        var currentLength = 0L
        var count = 0
        val buffer = ByteArray(Constants.READ_BUFFER_SIZE)
        BufferedOutputStream(FileOutputStream(fileSavePath)).use { out ->
            while (true) {
                val readSize = input.read(buffer)
                if (readSize == -1) break
                out.write(buffer, 0, readSize)
                currentLength += readSize
                count++
                if (count % 10 == 0) {
                    fs.currentSize = currentLength
                    fs.percent = (currentLength.toFloat() / length.toFloat() * 100).toInt()
                    if (fs.type == DBConstants.FILE_TYPE_VIDEO) notifyProgress(fs)
                }
            }
        }

        if (fs.type == DBConstants.FILE_TYPE_VIDEO) {
            fs.percent = 100
            notifyProgress(fs)
        }
        WifiChatApplication.recieveFileStates.remove(fs.fileName)
    }

    private fun notifyProgress(fs: FileState) {
        handler?.obtainMessage()?.apply {
            obj = fs
            sendToTarget()
        }
    }

    companion object {
        @Volatile
        private var instance: TcpService? = null

        @Volatile
        private var handler: Handler? = null

        fun getInstance(@Suppress("UNUSED_PARAMETER") context: Context): TcpService =
            instance ?: synchronized(this) {
                instance ?: TcpService().also { instance = it }
            }

        fun setHandler(paramHandler: Handler) {
            handler = paramHandler
        }
    }
}
