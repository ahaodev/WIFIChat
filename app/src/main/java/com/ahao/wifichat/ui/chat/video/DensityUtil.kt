package com.ahao.wifichat.ui.chat.video

import android.content.Context

/** 屏幕尺寸与单位换算 */
object DensityUtil {

    fun getWidthInPx(context: Context): Float = context.resources.displayMetrics.widthPixels.toFloat()

    fun getHeightInPx(context: Context): Float = context.resources.displayMetrics.heightPixels.toFloat()

    fun dip2px(context: Context, dpValue: Float): Int =
        (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
}
