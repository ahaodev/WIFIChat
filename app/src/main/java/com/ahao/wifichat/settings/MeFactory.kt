package com.ahao.wifichat.settings

import com.ahao.wifichat.misc.User
import com.ahao.wifichat.utlis.Tools

/** 创建应用用户 `me` */
object MeFactory {

    /**
     * 随机唯一码、系统用户名作为昵称（无效时用唯一码）、登录时间与操作系统名称。
     */
    fun createMe(): User {
        val code = createUniqueCode()
        return User(nick = createNickName(code), code = code).apply {
            isMe = true
            lastIdle = System.currentTimeMillis()
            logonTime = System.currentTimeMillis()
            operatingSystem = System.getProperty("os.name") ?: operatingSystem
        }
    }

    /** 10000000 ~ 20000000 之间的随机数 */
    private fun createUniqueCode(): Int = 10_000_000 + (Math.random() * 9_999_999).toInt()

    /** 取系统用户名的第一段，截断到 10 位并首字母大写；不合法则使用唯一码 */
    private fun createNickName(code: Int): String {
        val userName = System.getProperty("user.name") ?: return code.toString()
        val firstPart = userName.split(" ")[0].trim()
        val defaultNick = Tools.capitalizeFirstLetter(Tools.shorten(firstPart, 10))
        return if (Tools.isValidNick(defaultNick)) defaultNick else code.toString()
    }
}
