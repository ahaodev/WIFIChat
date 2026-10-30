package com.ahao.wifichat.event

import com.ahao.wifichat.settings.Setting

/** 设置项变化回调 */
fun interface SettingsListener {
    fun settingChanged(setting: Setting)
}
