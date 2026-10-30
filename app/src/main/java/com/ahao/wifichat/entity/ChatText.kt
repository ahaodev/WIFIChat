package com.ahao.wifichat.entity

import com.google.gson.Gson

/** 文本消息正文 */
data class ChatText(var text: String? = null) {
    override fun toString(): String = Gson().toJson(this)
}
