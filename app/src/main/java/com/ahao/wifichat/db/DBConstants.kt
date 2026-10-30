package com.ahao.wifichat.db

object DBConstants {
    /** 消息类型 */
    const val MSG_TYPE_TXT = 1
    const val MSG_TYPE_LOCATION = 2
    const val MSG_TYPE_FILE = 3

    /** 消息方向 */
    const val DIR_TYPE_I = 1 // 当前用户发送
    const val DIR_TYPE_Y = 2 // 接收到的消息

    /** 文件类型 */
    const val FILE_TYPE_IMG = 1
    const val FILE_TYPE_VIDEO = 2
    const val FILE_TYPE_AUDIO = 3
    const val FILE_TYPE = 4

    /** 文件下载状态 */
    const val DOWNLOAD_OK = 1
    const val DOWNLOAD_ERR = 0

    /** 发送状态 */
    const val SEND_STATUS_ING = 0 // 正在发送
    const val SEND_STATUS_ERR = 1 // 发送错误
    const val SEND_STATUS_OK = 2  // 对方收到

    /** 未读 / 已读 */
    const val READ_N = 0
    const val READ_Y = 1
}
