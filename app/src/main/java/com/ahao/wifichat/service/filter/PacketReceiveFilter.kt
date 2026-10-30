package com.ahao.wifichat.service.filter

import com.ahao.wifichat.protocol.codec.Pack

/** 按命令字过滤数据包 */
class PacketReceiveFilter(private val cmd: Int) : PacketFilter {
    override fun accept(packet: Pack): Boolean = packet.command == cmd
}
