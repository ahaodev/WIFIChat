package com.ahao.wifichat.service

import com.ahao.wifichat.protocol.P2pVersion
import com.ahao.wifichat.protocol.SerialNumberUtils
import com.ahao.wifichat.protocol.codec.Pack

abstract class IBasePackProtocol {
    protected fun createPack(sendUserName: String, command: Int, content: String): Pack =
        Pack(
            version = P2pVersion.ONE,
            sn = SerialNumberUtils.next(),
            sendName = sendUserName,
            command = command,
            content = content,
        )
}
