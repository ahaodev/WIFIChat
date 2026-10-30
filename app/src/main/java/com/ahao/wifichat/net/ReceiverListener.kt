package com.ahao.wifichat.net

/** 收消息回调 */
fun interface ReceiverListener {
    fun messageArrived(message: String, ipAddress: String, port: Int)
}
