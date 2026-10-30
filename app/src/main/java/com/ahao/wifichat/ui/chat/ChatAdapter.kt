package com.ahao.wifichat.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.ahao.wifichat.R
import com.ahao.wifichat.db.ChatMessageRow
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.utlis.StringUtils
import com.ahao.wifichat.widget.ChatMessageView

/** 聊天消息列表：自己发送的使用右侧布局，收到的使用左侧布局 */
class ChatAdapter(
    private val context: Context,
    private var rows: List<ChatMessageRow>,
) : BaseAdapter() {

    override fun getCount(): Int = rows.size

    override fun getItem(position: Int): ChatMessageRow = rows[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = rows[position].dirType

    fun notifyDataSetChanged(newRows: List<ChatMessageRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = rows[position]
        val inflater = LayoutInflater.from(context)
        val timeText = StringUtils.formatTime(row.msgTime ?: 0L)

        return if (row.dirType == DBConstants.DIR_TYPE_I) {
            inflater.inflate(R.layout.p2p_list_item_wechat_out, null).apply {
                findViewById<TextView>(R.id.tvtime).text = timeText
                val progress = findViewById<ProgressBar>(R.id.pbMsg)
                when (row.status) {
                    DBConstants.SEND_STATUS_OK -> progress.visibility = View.GONE
                    DBConstants.SEND_STATUS_ERR -> {
                        findViewById<ImageView>(R.id.ivRetry).visibility = View.VISIBLE
                        progress.visibility = View.GONE
                    }
                }
                findViewById<ChatMessageView>(R.id.tvcontent).setMessage(row)
            }
        } else {
            inflater.inflate(R.layout.p2p_list_item_wechat_in, null).apply {
                findViewById<TextView>(R.id.tvtime).text = timeText
                findViewById<ChatMessageView>(R.id.tvcontent).setMessage(row)
            }
        }
    }
}
