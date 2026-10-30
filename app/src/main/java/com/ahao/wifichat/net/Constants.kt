package com.ahao.wifichat.net

import java.io.File

/** 网络相关常量（后期可与 protocol 包整合） */
object Constants {
    const val APP_NAME = "WIFI-Chat"

    /** 广播 IP */
    const val NETWORK_BROADCAST_IP = "255.255.255.255"

    /** UDP 接收端口 */
    const val NETWORK_UDP_PORT = 12425

    /** 用于发送的临时多播地址和接收数据包 */
    const val NETWORK_TEMP_IP = "224.168.5.250"

    /** 用于发送和接收的临时组播 UDP 端口 */
    const val NETWORK_TEMP_PORT = 50056

    /** 数据包大小 */
    const val NETWORK_PACKET_SIZE = 1024

    /** 消息编码 */
    const val MESSAGE_CHARSET = "UTF-8"

    /** 私聊 UDP 端口的起始值，若被占用则依次 +1 尝试 */
    const val NETWORK_PRIVCHAT_PORT = 40656

    const val APP_VERSION = "0.0.1"

    /** 群聊组播地址 */
    const val NETWORK_IP = "224.168.5.200"

    /** 群聊组播端口 */
    const val NETWORK_CHAT_PORT = 40555

    /** 应用文件目录 */
    val APP_FOLDER: String =
        System.getProperty("user.home") + File.separator + ".${APP_NAME.lowercase()}" + File.separator

    /** 日志目录 */
    val APP_LOG_FOLDER: String = APP_FOLDER + "logs" + File.separator

    const val TCP_SERVER_RECEIVE_PORT = 5557

    /** 文件流缓冲大小 */
    const val READ_BUFFER_SIZE = 1024 * 4
}
