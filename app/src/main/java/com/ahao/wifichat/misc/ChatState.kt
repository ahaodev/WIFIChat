package com.ahao.wifichat.misc

/** 聊天状态信息 */
class ChatState {
    /** 当前话题 */
    val topic = Topic()

    /** 应用用户在最近一次更新时是否正在输入 */
    var isWrote = false

    /** 是否已登录聊天 */
    var isLoggedOn = false

    /** 登录流程是否已完成 */
    var isLogonCompleted = false
}
