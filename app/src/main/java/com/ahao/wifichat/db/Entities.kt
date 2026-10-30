package com.ahao.wifichat.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 消息表：一条消息对应一行。
 * 表名与列名沿用原 LitePal 库的结构，便于迁移旧数据。
 */
@Entity(tableName = "msg", indices = [Index("msg_att_id")])
data class MsgEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: Int = 0,
    val content: String? = null,
    @ColumnInfo(name = "msg_time") val msgTime: Long = 0L,
    @ColumnInfo(name = "msg_att_id") val msgAttId: Int? = null,
)

/** 用户-消息关系表：每条消息对应一个“自己”视角的记录 */
@Entity(
    tableName = "user_msg",
    indices = [Index("message_id"), Index("username", "other_username")],
)
data class UserMsgEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String? = null,
    @ColumnInfo(name = "dir_type") val dirType: Int = 0,
    @ColumnInfo(name = "other_username") val otherUsername: String? = null,
    @ColumnInfo(name = "other_name") val otherName: String? = null,
    val status: Int = 0,
    @ColumnInfo(name = "is_read") val isRead: Int = 0,
    @ColumnInfo(name = "message_id") val messageId: Int? = null,
)

/** 附件表 */
@Entity(tableName = "msg_attachment", indices = [Index("file_uuid")])
data class MsgAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "file_uuid") val fileUuid: String? = null,
    val type: Int = 0,
    val filename: String? = null,
    val size: Long = 0L,
    val uri: String? = null,
    val status: Int = 0,
)
