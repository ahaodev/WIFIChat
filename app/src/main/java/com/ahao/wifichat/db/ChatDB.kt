package com.ahao.wifichat.db

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.net.file.FileState
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.utlis.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 聊天数据仓库。读取返回 Flow（数据变化自动推送），写入为 suspend 函数。
 * 首次启动时会把旧 LitePal 库（WiFIChat.db）的数据迁移到 Room。
 */
object ChatDB {

    private const val TAG = "ChatDB"
    private const val PREFS_MIGRATION = "db_migration"
    private const val KEY_LEGACY_MIGRATED = "litepal_migrated"

    private lateinit var database: ChatDatabase

    private val dao: ChatDao get() = database.chatDao()

    private val me: String get() = WiFiChat.instance.me.username.orEmpty()

    fun init(context: Context) {
        database = ChatDatabase.get(context)
        AppScope.io.launch {
            runCatching { migrateLegacyIfNeeded(context) }
                .onFailure { Log.e(TAG, "迁移旧数据库失败", it) }
        }
    }

    fun observeChatList(): Flow<List<ChatListRow>> = dao.observeChatList(me)

    fun observeConversation(otherUsername: String): Flow<List<ChatMessageRow>> =
        dao.observeConversation(me, otherUsername)

    fun observeUnreadTotal(): Flow<Int> = dao.observeUnreadTotal(me)

    /**
     * 插入一条消息（以及可选的附件），返回消息 id。
     * [other] 为空时（对方不在线的异常情况）只记录消息本身。
     */
    suspend fun insertMessage(
        dirType: Int,
        content: String?,
        type: Int,
        time: Long,
        username: String,
        other: OnlineUserInfo?,
        file: FileState? = null,
        filePath: String? = null,
        uuid: String? = null,
        attachmentStatus: Int = DBConstants.DOWNLOAD_ERR,
    ): Int = database.withTransaction {
        val attachmentId = file?.let {
            dao.insertAttachment(
                MsgAttachmentEntity(
                    fileUuid = uuid,
                    type = it.type,
                    filename = it.fileName,
                    size = it.fileSize,
                    uri = filePath,
                    status = attachmentStatus,
                ),
            ).toInt()
        }
        val msgId = dao.insertMsg(
            MsgEntity(type = type, content = content, msgTime = time, msgAttId = attachmentId),
        ).toInt()
        val outgoing = dirType == DBConstants.DIR_TYPE_I
        dao.insertUserMsg(
            UserMsgEntity(
                username = username,
                dirType = dirType,
                otherUsername = other?.userName,
                otherName = other?.name,
                status = if (outgoing) DBConstants.SEND_STATUS_ING else 0,
                isRead = DBConstants.READ_N,
                messageId = msgId,
            ),
        )
        msgId
    }

    /** 将 [time] 之前仍在“发送中”的消息标记为发送失败 */
    suspend fun updateSendTimeOut(otherUsername: String, time: Long) {
        dao.failTimedOutSends(me, otherUsername, time)
    }

    /** 将与对方的消息全部标记为已读 */
    suspend fun updateReadStatus(otherUsername: String?) {
        val myName = me
        if (otherUsername.isNullOrEmpty() || myName.isEmpty()) return
        dao.markConversationRead(myName, otherUsername)
    }

    /** 更新发送状态：0 发送中，1 发送失败，2 对方已收到 */
    suspend fun updateSendStatus(myName: String, otherUsername: String, msgId: Int, status: Int) {
        dao.updateSendStatus(myName, otherUsername, msgId, status)
    }

    suspend fun updateDownloadStatus(fileUuid: String, status: Int) {
        dao.updateDownloadStatus(fileUuid, status)
    }

    /**
     * 把 LitePal 的三张表（msg / user_msg / msg_attachment）原样复制到 Room 库。
     * 只执行一次；旧库不存在时直接标记完成。
     */
    private suspend fun migrateLegacyIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_MIGRATION, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_LEGACY_MIGRATED, false)) return

        val legacyFile = context.getDatabasePath(ChatDatabase.LEGACY_DB_NAME)
        if (legacyFile.exists()) {
            val sqlite = database.openHelper.writableDatabase
            sqlite.execSQL("ATTACH DATABASE '${legacyFile.absolutePath}' AS legacy")
            try {
                database.withTransaction {
                    val attExpr = if (hasLegacyColumn("msg", "msg_att_id")) "msg_att_id" else "NULL"
                    val msgExpr = if (hasLegacyColumn("user_msg", "message_id")) "message_id" else "NULL"
                    sqlite.execSQL(
                        "INSERT OR IGNORE INTO msg_attachment (id, file_uuid, type, filename, size, uri, status) " +
                            "SELECT id, file_uuid, type, filename, size, uri, status FROM legacy.msg_attachment",
                    )
                    sqlite.execSQL(
                        "INSERT OR IGNORE INTO msg (id, type, content, msg_time, msg_att_id) " +
                            "SELECT id, type, content, msg_time, $attExpr FROM legacy.msg",
                    )
                    sqlite.execSQL(
                        "INSERT OR IGNORE INTO user_msg (id, username, dir_type, other_username, other_name, " +
                            "status, is_read, message_id) SELECT id, username, dir_type, other_username, " +
                            "other_name, status, is_read, $msgExpr FROM legacy.user_msg",
                    )
                }
            } finally {
                sqlite.execSQL("DETACH DATABASE legacy")
            }
        }
        prefs.edit().putBoolean(KEY_LEGACY_MIGRATED, true).apply()
    }

    /** 查询旧库某表是否包含某列（旧结构中部分关联列可能不存在） */
    private fun hasLegacyColumn(table: String, column: String): Boolean {
        val cursor = database.openHelper.writableDatabase.query("PRAGMA legacy.table_info($table)")
        cursor.use {
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndexOrThrow("name")) == column) return true
            }
        }
        return false
    }
}
