package com.ahao.wifichat.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MsgEntity::class, UserMsgEntity::class, MsgAttachmentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    companion object {
        /** 新库文件名；旧的 LitePal 库为 `WiFIChat.db` */
        const val DB_NAME = "WiFiChatRoom.db"
        const val LEGACY_DB_NAME = "WiFIChat.db"

        @Volatile
        private var instance: ChatDatabase? = null

        fun get(context: Context): ChatDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, ChatDatabase::class.java, DB_NAME)
                    .build()
                    .also { instance = it }
            }
    }
}
