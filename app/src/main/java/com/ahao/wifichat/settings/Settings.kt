package com.ahao.wifichat.settings

import com.ahao.wifichat.event.SettingsListener
import com.ahao.wifichat.misc.User
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.utlis.Tools
import kotlin.properties.Delegates

/** 应用设置 */
class Settings {

    private val listeners = mutableListOf<SettingsListener>()

    /** 应用用户，仅存储昵称等信息 */
    val me: User = MeFactory.createMe()

    var ownColor: Int = -15987646
    var sysColor: Int = -16759040
    var isSound: Boolean = true
    var isSmileys: Boolean = true
    var isBalloons: Boolean = false
    var browser: String = ""
    var lookAndFeel: String = ""

    /** 网络接口名，null 表示自动选择 */
    var networkInterface: String? = null

    /** 是否禁用私聊（启动参数） */
    var isNoPrivateChat: Boolean = false

    /** 是否始终记录日志（启动参数） */
    var isAlwaysLog: Boolean = false

    /** 日志存放目录，null 表示使用默认目录 */
    var logLocation: String? = null

    /** 值变化时通知监听器 */
    var logging: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) fireSettingChanged(Setting.LOGGING)
    }

    val isLogging: Boolean get() = isAlwaysLog || logging

    /** 实际生效的日志目录 */
    val effectiveLogLocation: String
        get() = logLocation?.takeIf { it.isNotEmpty() }?.let(Tools::appendSlash) ?: Constants.APP_LOG_FOLDER

    /** 设置对外报告的客户端类型，需在登录网络前调用 */
    fun setClient(client: String) {
        me.client = "${Constants.APP_NAME} v${Constants.APP_VERSION} $client"
    }

    private fun fireSettingChanged(setting: Setting) {
        listeners.forEach { it.settingChanged(setting) }
    }

    fun addSettingsListener(listener: SettingsListener) {
        listeners += listener
    }

    fun removeSettingsListener(listener: SettingsListener) {
        listeners -= listener
    }
}
