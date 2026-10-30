package com.ahao.wifichat.protocol

object P2pCmd {
    const val CMD_BR_ENTRY = 0x00000001        // 上线通知（广播）
    const val CMD_ANS_ENTRY = 0x00000002       // 确认收到上线通知
    const val CMD_BR_EXIT = 0x00000003         // 下线通知（广播）
    const val CMD_SEND_TEXT = 0x00000004       // 发送文本消息
    const val CMD_SEND_LOC = 0x00000005        // 发送位置消息
    const val CMD_BR_LOC = 0x00000006          // 广播位置
    const val CMD_FILE_NOTICE = 0x00000007     // 发送附件通知消息
    const val CMD_RECV_MSG = 0x00000009        // 确认收到消息
    const val CMD_GET_FILE_DATA = 0x000000F0   // 下载文件
}
