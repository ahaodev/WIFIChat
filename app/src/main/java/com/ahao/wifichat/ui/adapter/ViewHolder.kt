package com.ahao.wifichat.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ahao.wifichat.utlis.GlideLoder

/** 列表项视图缓存，支持链式调用 */
class ViewHolder private constructor(
    private val context: Context,
    parent: ViewGroup?,
    layoutId: Int,
    var position: Int,
) {
    private val views = SparseArray<View>()
    private val imageLoader = GlideLoder<Any>(context)

    val convertView: View = LayoutInflater.from(context).inflate(layoutId, parent, false).also { it.tag = this }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> getView(viewId: Int): T {
        val view = views.get(viewId) ?: convertView.findViewById<View>(viewId).also { views.put(viewId, it) }
        return view as T
    }

    fun setText(viewId: Int, text: CharSequence?): ViewHolder = apply { getView<TextView>(viewId).text = text }

    fun setText(viewId: Int, resStrId: Int): ViewHolder = apply { getView<TextView>(viewId).setText(resStrId) }

    fun setTextColor(viewId: Int, color: Int): ViewHolder = apply { getView<TextView>(viewId).setTextColor(color) }

    fun setTag(viewId: Int, tag: Any?): ViewHolder = apply { getView<View>(viewId).tag = tag }

    fun setImageResource(viewId: Int, drawableId: Int): ViewHolder = apply {
        getView<ImageView>(viewId).setImageResource(drawableId)
    }

    fun setImageBitmap(viewId: Int, bm: Bitmap): ViewHolder = apply { getView<ImageView>(viewId).setImageBitmap(bm) }

    fun setImageByUrl(viewId: Int, url: String): ViewHolder = apply {
        imageLoader.loadImage(getView(viewId), url)
    }

    fun setBackgroundResource(viewId: Int, drawable: Int): ViewHolder = apply {
        getView<View>(viewId).setBackgroundResource(drawable)
    }

    fun setOnClick(viewId: Int, listener: View.OnClickListener): ViewHolder = apply {
        getView<View>(viewId).setOnClickListener(listener)
    }

    fun setVisibility(viewId: Int, visibility: Int): ViewHolder = apply { getView<View>(viewId).visibility = visibility }

    fun getVisibility(viewId: Int): Int = getView<View>(viewId).visibility

    fun goNone(viewId: Int): ViewHolder = setVisibility(viewId, View.GONE)

    fun goShow(viewId: Int): ViewHolder = setVisibility(viewId, View.VISIBLE)

    fun getText(viewId: Int): String = getView<TextView>(viewId).text.toString()

    companion object {
        fun get(context: Context, convertView: View?, parent: ViewGroup?, layoutId: Int, position: Int): ViewHolder =
            if (convertView == null) {
                ViewHolder(context, parent, layoutId, position)
            } else {
                (convertView.tag as ViewHolder).apply { this.position = position }
            }
    }
}
