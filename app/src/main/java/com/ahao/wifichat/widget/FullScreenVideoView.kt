package com.ahao.wifichat.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

/** 按视频原始比例铺满可用空间的 VideoView */
class FullScreenVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : VideoView(context, attrs, defStyle) {

    var videoWidth: Int = 0
    var videoHeight: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(videoWidth, widthMeasureSpec)
        var height = getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {
            if (videoWidth * height > width * videoHeight) {
                height = width * videoHeight / videoWidth
            } else if (videoWidth * height < width * videoHeight) {
                width = height * videoWidth / videoHeight
            }
        }
        setMeasuredDimension(width, height)
    }
}
