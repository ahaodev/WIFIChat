package com.ahao.wifichat.net

import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import java.util.concurrent.Callable

/** 通过 UDP 向指定用户发送一条已编码的数据 */
class SendDataUDP(
    private val networkService: NetworkService,
    private val destUser: OnlineUserInfo,
    private val privMsg: String,
) : Callable<Boolean> {
    override fun call(): Boolean =
        networkService.sendUDPMsg(privMsg, destUser.ipAdderss, destUser.port)
}
