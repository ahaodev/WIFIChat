package com.ahao.wifichat.protocol.constant

enum class EnumMsgType(val value: Int, val tip: String) {
    TXT(1, "文本"),
    POS(2, "位置"),
    ATTACHMENT(3, "附件");

    companion object {
        fun fromValue(value: Int): EnumMsgType? = entries.firstOrNull { it.value == value }
    }

    override fun toString(): String = "$tip[$value]"
}
