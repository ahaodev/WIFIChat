package com.ahao.wifichat.service

import android.content.Context
import android.media.RingtoneManager
import android.os.Vibrator
import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.entity.Userinfo
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.net.DataConnection
import com.ahao.wifichat.net.file.FileState
import com.ahao.wifichat.protocol.P2pCmd
import com.ahao.wifichat.protocol.codec.Pack
import com.ahao.wifichat.protocol.constant.EnumMsgType
import com.ahao.wifichat.protocol.v1.BRLoaction
import com.ahao.wifichat.protocol.v1.Entry
import com.ahao.wifichat.protocol.v1.MsgAttachmentContent
import com.ahao.wifichat.protocol.v1.MsgLocation
import com.ahao.wifichat.protocol.v1.MsgText
import com.ahao.wifichat.service.filter.PacketReceiveFilter
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.service.parcelable.ReplyMessage
import com.ahao.wifichat.service.parcelable.UserLocation
import com.ahao.wifichat.service.parcelable.UserMessage
import com.ahao.wifichat.utlis.AppScope
import com.ahao.wifichat.utlis.GsonUtils
import com.ahao.wifichat.utlis.toJson
import kotlinx.coroutines.launch

/**
 * 解析接收到的数据包：更新在线状态、写入数据库、回复确认，并通过 [ChatEvents] 通知界面。
 * 在网络接收线程上调用，耗时的数据库写入放到 [AppScope.io] 中执行。
 */
class ProcessReceiverData(private val dataConnection: DataConnection) {

    private val service: P2PService = dataConnection.service
    private val selfUser: Userinfo = WiFiChat.instance.me

    private val selfName: String get() = selfUser.username.orEmpty()

    init {
        register(P2pCmd.CMD_BR_ENTRY, ::onEntry)
        register(P2pCmd.CMD_ANS_ENTRY, ::onAnswerEntry)
        register(P2pCmd.CMD_BR_EXIT, ::onExit)
        register(P2pCmd.CMD_SEND_TEXT, ::onText)
        register(P2pCmd.CMD_SEND_LOC, ::onLocation)
        register(P2pCmd.CMD_FILE_NOTICE, ::onAttachment)
        register(P2pCmd.CMD_BR_LOC, ::onBroadcastLocation)
        register(P2pCmd.CMD_RECV_MSG, ::onReceipt)
    }

    private fun register(cmd: Int, handler: (Pack) -> Unit) {
        dataConnection.addPacketListener(PacketReceiveFilter(cmd)) { packet -> handler(packet) }
    }

    /** 上线通知（广播）：记录用户并回复自己的信息 */
    private fun onEntry(packet: Pack) {
        val userInfo = parseEntry(packet) ?: return
        service.addOnlineUser(userInfo)
        ChatEvents.emit(ChatEvent.OnlineUserUpdate(userInfo))
        dataConnection.sendData(PackCommand.packReplyEntry(selfName, selfUser.name, selfUser.ipAdderss))
    }

    /** 上线通知回复 */
    private fun onAnswerEntry(packet: Pack) {
        val userInfo = parseEntry(packet) ?: return
        service.addOnlineUser(userInfo)
        ChatEvents.emit(ChatEvent.OnlineUserUpdate(userInfo))
    }

    private fun parseEntry(packet: Pack): OnlineUserInfo? {
        if (packet.content.isEmpty()) return null
        val entry = GsonUtils.getJson<Entry>(packet.content) ?: return null
        return OnlineUserInfo(
            userName = packet.sendName,
            name = entry.name,
            ipAdderss = entry.ipAdderss,
            port = Constants.NETWORK_UDP_PORT,
        )
    }

    /** 下线通知 */
    private fun onExit(packet: Pack) {
        val userInfo = OnlineUserInfo(userName = packet.sendName)
        service.delOnlineUser(userInfo.userName)
        ChatEvents.emit(ChatEvent.OnlineUserUpdate(userInfo))
    }

    /** 文本消息 */
    private fun onText(packet: Pack) {
        if (packet.content.isEmpty()) return
        val data = GsonUtils.fromJsonObject<MsgText>(packet.content) ?: return
        val text = data.content ?: return

        val time = System.currentTimeMillis()
        val user = service.getOnlineUser(packet.sendName)
        AppScope.io.launch {
            ChatDB.insertMessage(DBConstants.DIR_TYPE_Y, text.text, DBConstants.MSG_TYPE_TXT, time, selfName, user)
            ChatEvents.emit(
                ChatEvent.NewMessage(
                    UserMessage(type = EnumMsgType.TXT, content = text.toJson(), msg_time = time, sendUsername = packet.sendName),
                ),
            )
            runVibrator()
            replyReceipt(user, packet, data.msg_id)
        }
    }

    /** 位置消息 */
    private fun onLocation(packet: Pack) {
        if (packet.content.isEmpty()) return
        val data = GsonUtils.fromJsonObject<MsgLocation>(packet.content) ?: return
        val loc = data.content ?: return

        val user = service.getOnlineUser(packet.sendName)
        val time = System.currentTimeMillis()
        AppScope.io.launch {
            ChatDB.insertMessage(DBConstants.DIR_TYPE_Y, loc.toJson(), DBConstants.MSG_TYPE_LOCATION, time, selfName, user)
            ChatEvents.emit(
                ChatEvent.NewMessage(
                    UserMessage(type = EnumMsgType.POS, content = loc.toJson(), msg_time = time, sendUsername = packet.sendName),
                ),
            )
            runVibrator()
            replyReceipt(user, packet, data.msg_id)
        }
    }

    /** 附件通知 */
    private fun onAttachment(packet: Pack) {
        if (packet.content.isEmpty()) return
        val data = GsonUtils.getJson<MsgAttachmentContent>(packet.content) ?: return
        val attachment = data.attachment ?: return

        val user = service.getOnlineUser(packet.sendName)
        val time = System.currentTimeMillis()
        val fileState = FileState(
            fileSize = attachment.size,
            currentSize = 1,
            fileName = attachment.name,
            type = attachment.type,
        )
        AppScope.io.launch {
            ChatDB.insertMessage(
                DBConstants.DIR_TYPE_Y, attachment.toJson(), EnumMsgType.ATTACHMENT.value, time,
                selfName, user, fileState, attachment.name, attachment.uuid, DBConstants.DOWNLOAD_OK,
            )
            ChatEvents.emit(
                ChatEvent.NewMessage(
                    UserMessage(
                        type = EnumMsgType.ATTACHMENT,
                        content = packet.content,
                        msg_time = time,
                        sendUsername = packet.sendName,
                    ),
                ),
            )
            runVibrator()
            replyReceipt(user, packet, data.msg_id)
        }
    }

    /** 实时位置广播 */
    private fun onBroadcastLocation(packet: Pack) {
        if (packet.content.isEmpty()) return
        val loc = GsonUtils.getJson<BRLoaction>(packet.content) ?: return
        val location = UserLocation(username = packet.sendName, lon = loc.lon, lat = loc.lat)
        service.addOnlineUserLocation(location)
        ChatEvents.emit(ChatEvent.LocationUpdate(location))
    }

    /** 消息已送达确认：更新发送状态（界面通过数据库 Flow 自动刷新） */
    private fun onReceipt(packet: Pack) {
        if (packet.content.isEmpty()) return
        val replyMsg = GsonUtils.getJson<ReplyMessage>(packet.content) ?: return
        AppScope.io.launch {
            ChatDB.updateSendStatus(selfName, packet.sendName, replyMsg.msg_id, DBConstants.SEND_STATUS_OK)
        }
    }

    private fun replyReceipt(user: OnlineUserInfo?, packet: Pack, msgId: Int) {
        if (user == null) return
        dataConnection.sendData(user, PackCommand.packReplyMessage(selfName, packet.command, packet.sn, msgId))
    }

    private fun runVibrator() {
        val ringtone = RingtoneManager.getRingtone(
            service.applicationContext,
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
        )
        ringtone.play()
        (service.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500) // 震半秒钟
    }
}
