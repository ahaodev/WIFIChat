package com.ahao.wifichat.ui.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ahao.wifichat.R
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.db.ChatListRow
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.ui.BaseFragment
import com.ahao.wifichat.ui.adapter.CommonAdapter
import com.ahao.wifichat.ui.adapter.ViewHolder
import com.ahao.wifichat.ui.chat.ChatActivity
import com.ahao.wifichat.utlis.StringUtils
import kotlinx.coroutines.launch

/** 会话列表：订阅数据库 Flow，数据变化时自动刷新 */
class ChatListFragment : BaseFragment() {

    private lateinit var listView: ListView
    private lateinit var chatAdapter: CommonAdapter<ChatListRow>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.p2p_fragment_chatlist, null)
        listView = v.findViewById(R.id.chat_fragment_listview)
        chatAdapter = object : CommonAdapter<ChatListRow>(requireActivity(), emptyList(), R.layout.item_chatlist) {
            override fun convert(helper: ViewHolder, item: ChatListRow?) {
                item ?: return
                val peer = item.otherUsername.orEmpty()
                helper.setText(R.id.tvcontacts, item.otherName)
                helper.setText(R.id.tvcontent, previewOf(item))
                helper.setText(R.id.tvtime, StringUtils.formatTime(item.time))

                val online = app.binder?.getUser(peer) != null
                helper.setBackgroundResource(R.id.chperson, if (online) R.drawable.wechat_down else R.drawable.wechat)

                if (item.unread > 0) {
                    helper.setVisibility(R.id.tvTip, View.VISIBLE)
                    helper.setText(R.id.tvTip, item.unread.toString())
                } else {
                    helper.goNone(R.id.tvTip)
                }

                helper.setOnClick(R.id.chatlist_click) {
                    // 不在线的用户仍可打开历史会话
                    val user = app.binder?.getUser(peer) ?: OnlineUserInfo(userName = peer, online = 0)
                    startActivity(ChatActivity::class.java, ChatActivity.USER, user)
                }
            }
        }
        listView.adapter = chatAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ChatDB.observeChatList().collect { rows -> chatAdapter.notifyDataSetChanged(rows) }
            }
        }
        return v
    }

    /** 多媒体消息在列表中显示占位文字 */
    private fun previewOf(row: ChatListRow): String? = when (row.type) {
        DBConstants.MSG_TYPE_LOCATION -> "[位置]"
        DBConstants.MSG_TYPE_FILE -> "[附件]"
        else -> row.content
    }
}
