package com.ahao.wifichat.utlis

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File

class GlideLoder<T>(private val context: Context) : ImageLoader<T> {

    fun loadImageByFile(imageView: ImageView, file: String) {
        Glide.with(context).load(File(file)).into(imageView)
    }

    override fun loadImage(imageView: ImageView, url: String) {
        Glide.with(context).load(url).into(imageView)
    }
}
