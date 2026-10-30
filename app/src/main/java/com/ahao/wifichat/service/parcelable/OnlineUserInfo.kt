package com.ahao.wifichat.service.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** 在线用户信息；下线时 [ipAdderss] 与 [port] 为空 */
@Parcelize
data class OnlineUserInfo(
    var userName: String = "",
    var name: String? = null,
    var ipAdderss: String? = null,
    var port: Int = 0,
    /** 0 不在线，1 在线 */
    var online: Int = 1,
) : Parcelable
