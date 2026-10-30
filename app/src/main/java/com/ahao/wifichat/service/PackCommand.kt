package com.ahao.wifichat.service

import com.ahao.wifichat.protocol.Fields
import com.ahao.wifichat.protocol.P2pCmd
import com.ahao.wifichat.protocol.codec.Pack
import com.ahao.wifichat.protocol.v1.Entry
import com.ahao.wifichat.protocol.v1.MessageInfo
import com.ahao.wifichat.protocol.v1.MsgAttachment
import com.ahao.wifichat.protocol.v1.MsgLocation
import com.ahao.wifichat.protocol.v1.MsgText
import com.ahao.wifichat.service.parcelable.ReplyMessage
import com.ahao.wifichat.utlis.toJson

/** 构造各类协议数据包 */
object PackCommand : IBasePackProtocol() {

    fun packSendEntry(username: String, name: String?, ipAdderss: String?): Pack =
        createPack(username, P2pCmd.CMD_BR_ENTRY, Entry(name = name, ipAdderss = ipAdderss).toJson())

    fun packReplyEntry(username: String, name: String?, ipAdderss: String?): Pack =
        createPack(username, P2pCmd.CMD_ANS_ENTRY, Entry(name = name, ipAdderss = ipAdderss).toJson())

    fun packSendExit(username: String): Pack = createPack(username, P2pCmd.CMD_BR_EXIT, "")

    fun packMessage(username: String, cmd: Int, msgId: Int, fields: Fields): Pack =
        createPack(username, cmd, MessageInfo<Fields>(msg_id = msgId, content = fields).toJson())

    fun packSendText(username: String, msgId: Int, content: String?): Pack =
        packMessage(username, P2pCmd.CMD_SEND_TEXT, msgId, MsgText(text = content))

    fun packSendLocation(username: String, msgId: Int, lon: Double, lat: Double, name: String?): Pack =
        packMessage(username, P2pCmd.CMD_SEND_LOC, msgId, MsgLocation(name = name, lon = lon, lat = lat))

    fun packBRLocation(username: String, lon: Double, lat: Double, name: String?): Pack =
        createPack(username, P2pCmd.CMD_BR_LOC, MsgLocation(name = name, lon = lon, lat = lat).toJson())

    fun packSendAttachment(
        username: String,
        msgId: Int,
        uuid: String?,
        type: Int,
        name: String?,
        size: Long,
    ): Pack = packMessage(
        username,
        P2pCmd.CMD_FILE_NOTICE,
        msgId,
        MsgAttachment(uuid = uuid, type = type, name = name, size = size),
    )

    fun packReplyMessage(username: String, cmd: Int, sn: Int, msgId: Int): Pack =
        createPack(
            username,
            P2pCmd.CMD_RECV_MSG,
            ReplyMessage(cmd = cmd, packet_no = sn, msg_id = msgId).toJson(),
        )
}
