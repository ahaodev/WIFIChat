package com.ahao.wifichat.ui.me

import android.os.Bundle
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.widget.TitleView

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        findViewById<TitleView>(R.id.title).setTitle("关于")
    }
}
