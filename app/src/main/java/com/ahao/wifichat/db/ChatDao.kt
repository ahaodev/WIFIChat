package com.ahao.wifichat.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** 会话列表的一行：每个对方用户取最新一条消息 */
data class ChatListRow(
    val otherUsername: String?,
    val otherName: String?,
    val type: Int,
    val content: String?,
    val time: Long,
    val unread: Int,
)

/** 会话消息的一行（user_msg 左连接 msg 与 msg_attachment） */
data class ChatMessageRow(
    val userMsgId: Int,
    val username: String?,
    val otherUsername: String?,
    val dirType: Int,
    val status: Int,
    val isRead: Int,
    val msgId: Int?,
    val type: Int?,
    val content: String?,
    val msgTime: Long?,
    val attId: Int?,
    val fileUuid: String?,
    val attType: Int?,
    val filename: String?,
    val size: Long?,
    val uri: String?,
    val attStatus: Int?,
)

@Dao
interface ChatDao {

    @Insert
    suspend fun insertMsg(msg: MsgEntity): Long

    @Insert
    suspend fun insertUserMsg(userMsg: UserMsgEntity): Long

    @Insert
    suspend fun insertAttachment(attachment: MsgAttachmentEntity): Long

    /** 会话列表：按时间倒序，附带未读数；数据变化时自动推送 */
    @Query(
        """
        SELECT u.other_username AS otherUsername, u.other_name AS otherName,
               m.type AS type, m.content AS content, m.msg_time AS time,
               (SELECT COUNT(*) FROM user_msg x
                 WHERE x.username = u.username AND x.other_username = u.other_username
                   AND x.dir_type = ${DBConstants.DIR_TYPE_Y} AND x.is_read = ${DBConstants.READ_N}) AS unread
          FROM user_msg u
          JOIN msg m ON u.message_id = m.id
         WHERE u.username = :me
           AND m.msg_time = (SELECT MAX(m2.msg_time) FROM user_msg u2 JOIN msg m2 ON u2.message_id = m2.id
                              WHERE u2.username = u.username AND u2.other_username = u.other_username)
         GROUP BY u.other_username
         ORDER BY time DESC
        """
    )
    fun observeChatList(me: String): Flow<List<ChatListRow>>

    /** 与某个用户的全部消息，按时间升序 */
    @Query(
        """
        SELECT u.id AS userMsgId, u.username AS username, u.other_username AS otherUsername,
               u.dir_type AS dirType, u.status AS status, u.is_read AS isRead,
               m.id AS msgId, m.type AS type, m.content AS content, m.msg_time AS msgTime,
               a.id AS attId, a.file_uuid AS fileUuid, a.type AS attType, a.filename AS filename,
               a.size AS size, a.uri AS uri, a.status AS attStatus
          FROM user_msg u
          LEFT JOIN msg m ON u.message_id = m.id
          LEFT JOIN msg_attachment a ON m.msg_att_id = a.id
         WHERE u.username = :me AND u.other_username = :other
         ORDER BY m.msg_time ASC, u.id ASC
        """
    )
    fun observeConversation(me: String, other: String): Flow<List<ChatMessageRow>>

    /** 当前用户全部未读的接收消息数 */
    @Query(
        "SELECT COUNT(*) FROM user_msg WHERE username = :me " +
            "AND dir_type = ${DBConstants.DIR_TYPE_Y} AND is_read = ${DBConstants.READ_N}"
    )
    fun observeUnreadTotal(me: String): Flow<Int>

    @Query("UPDATE user_msg SET is_read = ${DBConstants.READ_Y} WHERE username = :me AND other_username = :other")
    suspend fun markConversationRead(me: String, other: String)

    @Query(
        "UPDATE user_msg SET status = :status WHERE username = :me AND other_username = :other " +
            "AND message_id = :msgId"
    )
    suspend fun updateSendStatus(me: String, other: String, msgId: Int, status: Int)

    /** 发送超过 [time] 仍处于“发送中”的消息标记为失败 */
    @Query(
        "UPDATE user_msg SET status = ${DBConstants.SEND_STATUS_ERR} WHERE username = :me " +
            "AND other_username = :other AND status = ${DBConstants.SEND_STATUS_ING} " +
            "AND message_id IN (SELECT id FROM msg WHERE msg_time < :time)"
    )
    suspend fun failTimedOutSends(me: String, other: String, time: Long)

    @Query("UPDATE msg_attachment SET status = :status WHERE file_uuid = :uuid")
    suspend fun updateDownloadStatus(uuid: String, status: Int)
}
