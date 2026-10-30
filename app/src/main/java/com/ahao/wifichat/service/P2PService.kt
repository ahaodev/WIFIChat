package com.ahao.wifichat.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.ahao.wifichat.net.DataConnection
import com.ahao.wifichat.net.UsersListener
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.service.parcelable.UserLocation
import java.util.concurrent.ConcurrentHashMap

class P2PService : Service() {

    /** 在线人员位置，key 为用户名 */
    val onlineUsersLocation = ConcurrentHashMap<String, UserLocation>()

    /** 在线列表，key 为用户名 */
    val onlineUsers = ConcurrentHashMap<String, OnlineUserInfo>()

    /** 用户上下线回调 */
    var userListener: UsersListener? = null

    private lateinit var dataConnection: DataConnection
    private lateinit var chatManager: ChatManager

    private val binder = P2PServiceBindle()

    /** 供 Activity 绑定后访问服务内部状态 */
    inner class P2PServiceBindle : Binder() {
        val users: List<OnlineUserInfo> get() = onlineUsers.values.toList()

        val usersLocation: List<UserLocation> get() = onlineUsersLocation.values.toList()

        fun getUser(name: String): OnlineUserInfo? = getOnlineUser(name)

        fun onlineUsersClear() {
            onlineUsers.clear()
            onlineUsersLocation.clear()
        }

        val connection: DataConnection get() = dataConnection

        val manager: ChatManager get() = chatManager
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        dataConnection = DataConnection(this)
        chatManager = ChatManager(dataConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        dataConnection.onDestroy()
    }

    /** 添加在线用户；同一 IP 的旧用户会被剔除，避免下线失败 */
    @Synchronized
    fun addOnlineUser(user: OnlineUserInfo) {
        onlineUsers.entries.removeIf { (_, existing) ->
            user.ipAdderss == existing.ipAdderss && user.userName != existing.userName
        }
        onlineUsers.putIfAbsent(user.userName, user)
    }

    fun addOnlineUserLocation(location: UserLocation) {
        onlineUsersLocation[location.username.orEmpty()] = location
    }

    fun getOnlineUser(userName: String): OnlineUserInfo? = onlineUsers[userName]

    fun getOnlineUserLocation(userName: String): UserLocation? = onlineUsersLocation[userName]

    fun delOnlineUser(userName: String) {
        onlineUsers.remove(userName)
        onlineUsersLocation.remove(userName)
    }

    fun isOnlineUser(userName: String): Boolean = onlineUsers.containsKey(userName)
}
