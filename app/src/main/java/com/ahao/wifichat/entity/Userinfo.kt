package com.ahao.wifichat.entity

import android.os.Parcelable
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.utlis.NetworkUtils
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

/** 用户信息 */
@Parcelize
data class Userinfo(
    var username: String? = null,
    var name: String? = null,
    var ipAdderss: String? = NetworkUtils.getLocalIpAddress(),
    var port: Int = Constants.NETWORK_PRIVCHAT_PORT,
) : Parcelable {
    override fun toString(): String = Gson().toJson(this)
}
