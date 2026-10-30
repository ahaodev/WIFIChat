package com.ahao.wifichat.protocol.codec

import java.io.Serializable

/** 协议数据包 */
data class Pack(
    /** 版本号 */
    var version: Int = 0,
    /** 消息流水号 */
    var sn: Int = 0,
    /** 发送者用户名 */
    var sendName: String = "",
    /** 保留字段 */
    var temp: String = "",
    /** 命令字 */
    var command: Int = 0,
    /** 消息内容 */
    var content: String = "",
) : Serializable {
    companion object {
        private const val serialVersionUID = 8203179441328456641L
    }
}
