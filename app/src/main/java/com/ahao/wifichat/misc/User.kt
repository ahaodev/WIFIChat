package com.ahao.wifichat.misc

/**
 * 聊天中的用户。
 * 以 [code] 作为唯一标识参与相等性判断，按昵称忽略大小写排序。
 */
class User(
    var nick: String,
    val code: Int,
) : Comparable<User> {

    var awayMsg: String = ""
    var ipAddress: String = UNKNOWN
    var operatingSystem: String = UNKNOWN
    var client: String = UNKNOWN
    var hostName: String? = null
    var privateChatPort: Int = 0
    var lastIdle: Long = 0L
    var logonTime: Long = 0L
    var isWriting: Boolean = false
    var isAway: Boolean = false
    var isMe: Boolean = false
    var isNewPrivMsg: Boolean = false
    var isOnline: Boolean = true
    var isNewMsg: Boolean = false

    /** 重置部分状态 */
    fun reset() {
        awayMsg = ""
        isWriting = false
        isAway = false
        ipAddress = UNKNOWN
        hostName = null
        isNewMsg = false
        privateChatPort = 0
        isNewPrivMsg = false
    }

    override fun compareTo(other: User): Int = nick.compareTo(other.nick, ignoreCase = true)

    override fun equals(other: Any?): Boolean = this === other || (other is User && code == other.code)

    override fun hashCode(): Int = code

    override fun toString(): String = nick

    private companion object {
        const val UNKNOWN = "<unknown>"
    }
}
