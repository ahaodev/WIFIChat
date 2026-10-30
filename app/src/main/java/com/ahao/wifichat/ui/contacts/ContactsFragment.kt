package com.ahao.wifichat.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ahao.wifichat.R
import com.ahao.wifichat.service.ChatEvent
import com.ahao.wifichat.service.ChatEvents
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.ui.BaseFragment
import com.ahao.wifichat.ui.MainActivity
import com.ahao.wifichat.ui.adapter.CommonAdapter
import com.ahao.wifichat.ui.adapter.ViewHolder
import com.ahao.wifichat.ui.chat.ChatActivity
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/** 在线联系人列表：上下线事件到达时刷新 */
class ContactsFragment : BaseFragment() {

    companion object {
        const val TAG = "ContactsFragment"
    }

    private lateinit var listView: ListView
    private lateinit var adapter: CommonAdapter<OnlineUserInfo>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.p2p_fragment_chatlist, null)
        listView = v.findViewById(R.id.chat_fragment_listview)
        adapter = object : CommonAdapter<OnlineUserInfo>(requireActivity(), emptyList(), R.layout.p2p_item_devices) {
            override fun convert(helper: ViewHolder, item: OnlineUserInfo?) {
                item ?: return
                helper.setImageResource(R.id.icon, R.drawable.default_person)
                helper.setText(R.id.device_name, item.name)
                helper.setText(R.id.device_details, item.userName)
            }
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position) ?: return@setOnItemClickListener
            item.online = 1
            startActivity(ChatActivity::class.java, ChatActivity.USER, item)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ChatEvents.events.filterIsInstance<ChatEvent.OnlineUserUpdate>().collect { refreshUsers() }
            }
        }
        refreshUsers()
        return v
    }

    override fun onResume() {
        super.onResume()
        val fragmentTag = tag
        (activity as MainActivity).titleView.apply {
            setTitle(fragmentTag)
            setRightVisibility(View.INVISIBLE)
        }
        refreshUsers()
    }

    private fun refreshUsers() {
        app.binder?.users?.takeIf { it.isNotEmpty() }?.let { adapter.notifyDataSetChanged(it) }
    }
}
