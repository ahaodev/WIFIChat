package com.ahao.wifichat.net.file

import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.WifiChatApplication
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.protocol.v1.MsgAttachment
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.utlis.toJson
import com.orhanobut.logger.Logger
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.Socket
import java.util.UUID
import com.ahao.wifichat.utlis.AppScope
import kotlinx.coroutines.launch

/** TCP 文件发送：每个文件一个独立线程 */
object TcpClient {

    private var listener: FileClientSendListener? = null

    fun setOnFileListener(listener: FileClientSendListener) {
        this.listener = listener
    }

    /** 异步将 [filePath] 发送给 [user] */
    fun sendFile(filePath: String, user: OnlineUserInfo, type: Int) {
        WifiChatApplication.sendFileStates[filePath] = FileState(fileName = filePath)
        AppScope.io.launch { transfer(user, filePath, type) }
    }

    private suspend fun transfer(user: OnlineUserInfo, filePath: String, type: Int) {
        val me = WiFiChat.instance.me
        val fs = WifiChatApplication.sendFileStates[filePath] ?: return
        try {
            val file = File(filePath)
            Socket(user.ipAdderss, Constants.TCP_SERVER_RECEIVE_PORT).use { socket ->
                FileInputStream(file).use { fileInput ->
                    val fileSize = fileInput.available()
                    val dataOutput = DataOutputStream(socket.getOutputStream())
                    dataOutput.writeUTF("${file.name}!$fileSize!${me.username}!$type")

                    fs.fileSize = fileSize.toLong()
                    fs.type = type

                    val uuid = UUID.randomUUID().toString()
                    val att = MsgAttachment(uuid = uuid, type = fs.type, name = fs.fileName, size = fs.fileSize)
                    val msgId = ChatDB.insertMessage(
                        DBConstants.DIR_TYPE_I, att.toJson(), DBConstants.MSG_TYPE_FILE,
                        System.currentTimeMillis(), me.username.orEmpty(), user, fs, filePath, uuid, 0,
                    )
                    listener?.onStart(fs)

                    var length = 0L
                    val buffer = ByteArray(Constants.READ_BUFFER_SIZE)
                    while (true) {
                        val readSize = fileInput.read(buffer)
                        if (readSize == -1) break
                        length += readSize
                        dataOutput.write(buffer, 0, readSize)
                        fs.percent = (length * 100 / fileSize).toInt()
                        listener?.onProgress(fs.percent)
                    }
                    Logger.d("${fs.fileName}发送完毕")

                    WiFiChat.instance.binder?.manager?.sendFileUdp(user, uuid, fs, msgId)
                    listener?.onSuccess(fs)
                    dataOutput.close()
                }
            }
            WifiChatApplication.sendFileStates.remove(filePath)
        } catch (e: IOException) {
            Logger.d("建立客户端socket失败")
            e.printStackTrace()
            listener?.onError(e)
        }
    }
}
