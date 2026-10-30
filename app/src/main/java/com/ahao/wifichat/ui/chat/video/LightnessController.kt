package com.ahao.wifichat.ui.chat.video

import android.app.Activity
import android.view.WindowManager

/**
 * 屏幕亮度控制，仅作用于当前窗口（无需 WRITE_SETTINGS 等系统权限）。
 * 亮度取值 0 ~ 255，与系统设置保持一致的刻度。
 */
object LightnessController {

    /** 当前窗口亮度（0 ~ 255）；未覆盖时返回 -1 */
    fun getLightness(activity: Activity): Int {
        val brightness = activity.window.attributes.screenBrightness
        return if (brightness < 0) -1 else (brightness * 255).toInt()
    }

    fun setLightness(activity: Activity, value: Int) {
        val lp: WindowManager.LayoutParams = activity.window.attributes
        lp.screenBrightness = (if (value <= 0) 1 else value.coerceAtMost(255)) / 255f
        activity.window.attributes = lp
    }

    /** 恢复系统默认亮度（退出页面时调用） */
    fun resetLightness(activity: Activity) {
        val lp = activity.window.attributes
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        activity.window.attributes = lp
    }
}
