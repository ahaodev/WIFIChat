package com.ahao.wifichat.misc

/** 主聊天室的话题信息 */
class Topic(
    var topic: String = "",
    var nick: String = "",
    var time: Long = 0L,
) {
    val hasTopic: Boolean get() = topic.isNotEmpty()

    fun changeTopic(topic: String, nick: String, time: Long) {
        this.topic = topic
        this.nick = nick
        this.time = time
    }

    fun changeTopic(other: Topic) = changeTopic(other.topic, other.nick, other.time)

    fun resetTopic() = changeTopic("", "", 0L)

    override fun toString(): String = "$topic ($nick)"
}
