package com.ahao.wifichat.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/** 通用列表适配器：子类实现 [convert] 填充每一项 */
abstract class CommonAdapter<T>(
    protected val context: Context,
    protected var datas: List<T>?,
    private val itemLayoutId: Int,
) : BaseAdapter() {

    override fun getCount(): Int = datas?.size ?: 0

    override fun getItem(position: Int): T? = datas?.get(position)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder = getViewHolder(position, convertView, parent)
        convert(holder, getItem(position))
        return holder.convertView
    }

    abstract fun convert(helper: ViewHolder, item: T?)

    protected open fun getViewHolder(position: Int, convertView: View?, parent: ViewGroup?): ViewHolder =
        ViewHolder.get(context, convertView, parent, itemLayoutId, position)

    fun notifyDataSetChanged(newDatas: List<T>?) {
        if (newDatas == null) return
        datas = newDatas
        notifyDataSetChanged()
    }
}
