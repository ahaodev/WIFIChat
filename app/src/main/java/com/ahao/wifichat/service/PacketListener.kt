package com.ahao.wifichat.service

import com.ahao.wifichat.protocol.codec.Pack

/**
 * 处理接收到的数据包。
 * 所有监听器由同一线程调用，实现方不应长时间阻塞。
 */
fun interface PacketListener {
    fun processPacket(packet: Pack)
}
