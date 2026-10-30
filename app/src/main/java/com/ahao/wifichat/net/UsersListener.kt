package com.ahao.wifichat.net

import com.ahao.wifichat.entity.Userinfo

/** 用户上下线回调 */
interface UsersListener {
    /** 用户上线 */
    fun userAdded(user: Userinfo)

    /** 用户信息变化 */
    fun userChanged(user: Userinfo)

    /** 用户下线 */
    fun userRemoved(user: Userinfo)
}
