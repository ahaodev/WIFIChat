package com.ahao.wifichat.ui.me

import android.os.Bundle
import android.view.View
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.widget.TitleView

class SettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_setting)
        findViewById<TitleView>(R.id.title).setTitle("设置")

        findViewById<View>(R.id.btn_logout).setOnClickListener {
            app.binder?.manager?.sendOutLine()
            app.me.apply {
                name = ""
                port = 0
                ipAdderss = ""
                username = ""
            }
            startActivity(LoginActivity::class.java)
            finish()
            app.binder?.onlineUsersClear() // 清空在线人员以及位置
            app.activityManager.clear() // 销毁 Activity 栈
        }
        findViewById<View>(R.id.btn_about_wechat).setOnClickListener {
            startActivity(AboutActivity::class.java)
        }
    }
}
