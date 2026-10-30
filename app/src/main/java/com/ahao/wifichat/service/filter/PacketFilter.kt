package com.ahao.wifichat.service.filter

import com.ahao.wifichat.protocol.codec.Pack

fun interface PacketFilter {
    /** 数据包是否通过过滤 */
    fun accept(packet: Pack): Boolean
}
