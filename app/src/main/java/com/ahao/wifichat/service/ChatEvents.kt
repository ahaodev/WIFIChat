package com.ahao.wifichat.service

import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.service.parcelable.UserLocation
import com.ahao.wifichat.service.parcelable.UserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** 网络层推送给界面层的业务事件 */
sealed interface ChatEvent {
    /** 上下线变化 */
    data class OnlineUserUpdate(val user: OnlineUserInfo) : ChatEvent

    /** 新消息（文本、位置、附件） */
    data class NewMessage(val msg: UserMessage) : ChatEvent

    /** 实时位置广播 */
    data class LocationUpdate(val location: UserLocation) : ChatEvent
}

/** 进程内事件总线；界面用 `collect` 订阅，生命周期感知由调用方负责 */
object ChatEvents {
    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    fun emit(event: ChatEvent) {
        _events.tryEmit(event)
    }
}
