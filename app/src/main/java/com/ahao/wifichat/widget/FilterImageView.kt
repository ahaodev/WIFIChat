package com.ahao.wifichat.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

/** 按下时显示灰色滤镜的图片控件 */
class FilterImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : ImageView(context, attrs, defStyle) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> setFilter()
            MotionEvent.ACTION_UP -> {
                performClick()
                removeFilter()
            }
            MotionEvent.ACTION_CANCEL -> removeFilter()
        }
        return true
    }

    /** 优先作用于 src 图片，没有则作用于背景 */
    private fun targetDrawable(): Drawable? = drawable ?: background

    private fun setFilter() {
        targetDrawable()?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }

    private fun removeFilter() {
        targetDrawable()?.clearColorFilter()
    }
}
