package com.ahao.wifichat.utlis

import android.widget.ImageView

fun interface ImageLoader<T> {
    fun loadImage(imageView: ImageView, url: String)
}
