package com.ahao.wifichat.event

/** 网络消息到达回调 */
fun interface ReceiverListener {
    fun messageArrived(message: String, ipAddress: String)
}
