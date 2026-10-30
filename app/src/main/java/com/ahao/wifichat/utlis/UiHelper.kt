package com.ahao.wifichat.utlis

import android.content.Context
import android.content.SharedPreferences

object UiHelper {

    /** dp 转 px */
    fun dip2px(context: Context, dpValue: Float): Int =
        (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()

    /** px 转 dp */
    fun px2dip(context: Context, pxValue: Float): Int =
        (pxValue / context.resources.displayMetrics.density + 0.5f).toInt()

    fun getShareBooleanState(context: Context, name: String, state: Boolean): Boolean =
        context.prefs("STATE").getBoolean(name, state)

    fun setShareBooleanState(context: Context, name: String, state: Boolean) {
        context.prefs("STATE").edit().putBoolean(name, state).commit()
    }

    fun setShareData(context: Context, filename: String, key: String, value: String) {
        context.prefs(filename).edit().putString(key, value).commit()
    }

    fun getShareData(context: Context, key: String, filename: String): String =
        context.prefs(filename).getString(key, "") ?: ""

    fun getShareIntgerData(context: Context, key: String, filename: String, defInteger: Int): Int =
        context.prefs(filename).getInt(key, defInteger)

    private fun Context.prefs(name: String): SharedPreferences =
        getSharedPreferences(name, Context.MODE_PRIVATE)
}
