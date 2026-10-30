package com.ahao.wifichat.widget

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.ahao.wifichat.R

/** 以 Toast 形式显示音量圆环 */
class VolumnController(private val context: Context) {

    private var toast: Toast? = null
    private var volumnView: VolumnView? = null

    fun show(progress: Float) {
        val current = toast ?: createToast().also { toast = it }
        volumnView?.progress = progress
        current.show()
    }

    private fun createToast(): Toast {
        val layout = LayoutInflater.from(context).inflate(R.layout.vv, null)
        volumnView = layout.findViewById(R.id.volumnView)
        return Toast(context).apply {
            view = layout
            setGravity(Gravity.BOTTOM, 0, 100)
            duration = Toast.LENGTH_SHORT
        }
    }
}
