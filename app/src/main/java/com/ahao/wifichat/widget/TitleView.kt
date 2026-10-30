package com.ahao.wifichat.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.ahao.wifichat.R

/** 页面顶部标题栏：返回按钮、标题、右侧按钮/文字 */
class TitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    private val backBtn: Button
    private val rightBtn: ImageButton
    private val rightTxt: TextView
    private val titleText: TextView

    init {
        View.inflate(context, R.layout.p2p_title, this)
        backBtn = findViewById(R.id.btn_back)
        rightBtn = findViewById(R.id.rightBtn)
        rightTxt = findViewById(R.id.rightTxt)
        titleText = findViewById(R.id.header_text)
        backBtn.setOnClickListener { (getContext() as? Activity)?.finish() }
    }

    fun setBackVisibility(visibility: Int) {
        backBtn.visibility = visibility
    }

    fun setBackOnClickListener(listener: OnClickListener) {
        backBtn.setOnClickListener(listener)
    }

    fun setRightClickListener(listener: OnClickListener) {
        rightBtn.setOnClickListener(listener)
    }

    fun setRightResource(resId: Int) {
        rightBtn.setImageResource(resId)
    }

    fun setRightText(txtId: Int) {
        rightTxt.setText(txtId)
    }

    fun setRightText(text: CharSequence?) {
        rightTxt.text = text
    }

    fun setRightVisibility(visibility: Int) {
        rightBtn.visibility = visibility
        rightTxt.visibility = visibility
    }

    fun setTitle(resId: Int) {
        titleText.setText(resId)
    }

    fun setTitle(text: CharSequence?) {
        titleText.text = text
    }
}
