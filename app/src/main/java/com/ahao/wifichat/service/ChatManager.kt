package com.ahao.wifichat.service

import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.net.DataConnection
import com.ahao.wifichat.net.file.FileState
import com.ahao.wifichat.protocol.v1.MsgLocation
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.utlis.toJson

/** 聊天业务：发送文本、位置、上下线、附件通知 */
class ChatManager(private val dataConnection: DataConnection) {

    private val me = WiFiChat.instance.me
    private val username get() = me.username.orEmpty()

    suspend fun sendText(destUser: OnlineUserInfo, content: String): Boolean {
        val msgId = ChatDB.insertMessage(
            DBConstants.DIR_TYPE_I, content, DBConstants.MSG_TYPE_TXT,
            System.currentTimeMillis(), username, destUser,
        )
        return dataConnection.sendData(destUser, PackCommand.packSendText(username, msgId, content))
    }

    fun broadcastLocation(lon: Double, lat: Double, name: String?) {
        dataConnection.sendData(PackCommand.packBRLocation(username, lon, lat, name))
    }

    suspend fun sendLocation(destUser: OnlineUserInfo, lon: Double, lat: Double, name: String?) {
        val msgId = ChatDB.insertMessage(
            DBConstants.DIR_TYPE_I, MsgLocation(name = name, lon = lon, lat = lat).toJson(),
            DBConstants.MSG_TYPE_LOCATION, System.currentTimeMillis(), username, destUser,
        )
        dataConnection.sendData(destUser, PackCommand.packSendLocation(username, msgId, lon, lat, name))
    }

    fun sendEntry() {
        dataConnection.sendData(PackCommand.packSendEntry(username, me.name, me.ipAdderss))
    }

    fun sendOutLine() {
        dataConnection.sendData(PackCommand.packSendExit(username))
    }

    /** 发送附件通知；文件本体通过 TCP 传输 */
    fun sendFileUdp(dstUser: OnlineUserInfo, uuid: String, file: FileState, msgId: Int) {
        dataConnection.sendData(
            dstUser,
            PackCommand.packSendAttachment(username, msgId, uuid, file.type, file.fileName, file.fileSize),
        )
    }
}
