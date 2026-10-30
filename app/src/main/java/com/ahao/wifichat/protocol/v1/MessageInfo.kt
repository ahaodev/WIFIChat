package com.ahao.wifichat.protocol.v1

/** 协议消息外层包装：`{"msg_id": .., "content": ..}` */
data class MessageInfo<T>(
    var msg_id: Int = 0,
    var content: T? = null,
)
