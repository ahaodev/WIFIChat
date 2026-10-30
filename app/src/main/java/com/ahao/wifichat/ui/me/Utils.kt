package com.ahao.wifichat.ui.me

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import java.security.MessageDigest

object Utils {
    const val TAG = "Utils"

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService("connectivity") as? ConnectivityManager
        if (cm == null) {
            Log.w(TAG, "couldn't get connectivity manager")
            return false
        }
        return cm.allNetworkInfo.orEmpty().any { it.state == NetworkInfo.State.CONNECTED }
    }

    /** MD5，返回大写十六进制字符串 */
    fun md5(inputString: String): String = runCatching {
        MessageDigest.getInstance("MD5").digest(inputString.toByteArray())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
            .uppercase()
    }.onFailure { it.printStackTrace() }.getOrDefault("")
}
