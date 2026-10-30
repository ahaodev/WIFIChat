package com.ahao.wifichat.event

/** 网络连接状态变化回调 */
interface NetworkConnectionListener {
    /** 网络断开；[silent] 为 true 时不向用户提示 */
    fun networkWentDown(silent: Boolean)

    /** 网络即将可用，所有监听器都会先收到此回调，再收到 [networkCameUp] */
    fun beforeNetworkCameUp()

    /** 网络已就绪；[silent] 为 true 时不向用户提示 */
    fun networkCameUp(silent: Boolean)
}
