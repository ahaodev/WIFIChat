package com.ahao.wifichat.ui.chat.image

import android.app.Activity
import android.os.Bundle
import com.ahao.wifichat.R
import com.ahao.wifichat.utlis.GlideLoder
import cn.bluemobi.dylan.photoview.library.PhotoView

/** 全屏查看聊天图片（支持缩放） */
class ChatImagesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_images)
        val imageView: PhotoView = findViewById(R.id.chat_image)
        GlideLoder<Any>(this).loadImage(imageView, intent.getStringExtra("url").orEmpty())
    }
}
