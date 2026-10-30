package com.ahao.wifichat.service

import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.service.parcelable.UserLocation
import com.ahao.wifichat.service.parcelable.UserMessage

/** 业务层回调（上下线、消息） */
interface ProcessListener {
    /** 上下线回调 */
    fun onOnlineUserUpdate(userInfo: OnlineUserInfo)

    /** 新消息（文本等） */
    fun onNewMessage(msg: UserMessage)

    fun interface LocationListener {
        fun onLocationMessage(message: UserLocation)
    }
}
