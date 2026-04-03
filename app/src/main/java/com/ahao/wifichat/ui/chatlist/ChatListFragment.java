package com.ahao.wifichat.ui.chatlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ahao.wifichat.R;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.entity.ChatListMessage;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.ui.BaseFragment;
import com.ahao.wifichat.ui.MainActivity;
import com.ahao.wifichat.ui.adapter.CommonAdapter;
import com.ahao.wifichat.ui.adapter.ViewHolder;
import com.ahao.wifichat.ui.chat.ChatActivity;
import com.ahao.wifichat.utlis.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends BaseFragment {
	private MainActivity main;
	private View v;
	private ListView mListView;
	private CommonAdapter<ChatListMessage> chatAdapter;
	private List<ChatListMessage> users = new ArrayList<ChatListMessage>();

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.p2p_fragment_chatlist, null);
		init(v);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		main = (MainActivity) getActivity();
		main.getTitleView().setTitle(getTag());
		main.getTitleView().setRightVisibility(View.VISIBLE);
		main.getTitleView().setRightText("摄像头");
		main.getTitleView().setRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void init(View v) {
		mListView = (ListView) v.findViewById(R.id.chat_fragment_listview);
		chatAdapter = new CommonAdapter<ChatListMessage>(getActivity(), users, R.layout.item_chatlist) {

			@Override
			public void convert(ViewHolder helper, final ChatListMessage item) {
				// helper.setImageBitmap(R.id.chperson, null);
				helper.setText(R.id.tvcontacts, item.getName());
				helper.setText(R.id.tvcontent, item.getContent());
				helper.setText(R.id.tvtime, StringUtils.formatTime(item.getTime()));
				helper.setVisibility(R.id.tvTip, View.GONE);
				if (null == app.getBinder().getUser(item.getUsername())) {
					helper.setBackgroundResource(R.id.chperson, R.drawable.wechat);
				} else {
					helper.setBackgroundResource(R.id.chperson, R.drawable.wechat_down);
				}
				int read = ChatDB.getInstance().findMessageRead(WiFiChat.instance().getMe().getUsername(),
						item.getUsername());
				if (read > 0) {
					helper.setVisibility(R.id.tvTip, View.VISIBLE);
					helper.setText(R.id.tvTip, read + "");
				}
				// helper.setImageResource(R.id.iv_group_type,
				// R.drawable.sel_chatting_add_btn);
				helper.setOnclick(R.id.chatlist_click, new OnClickListener() {

					@Override
					public void onClick(View v) {
						OnlineUserInfo user = app.getBinder().getUser(item.getUsername());
						if (null == user) {// 不在线
							user = new OnlineUserInfo();
							user.setUserName(item.getUsername());
							user.setOnline(0);
						}
						startActivity(ChatActivity.class, ChatActivity.USER, user);
					}
				});
			}
		};
		mListView.setAdapter(chatAdapter);
		chatAdapter.notifyDataSetChanged(getChatListMessage());
	}

	public void notifyDate() {
		chatAdapter.notifyDataSetChanged(getChatListMessage());
	}

	private List<ChatListMessage> getChatListMessage() {
		users = ChatDB.getInstance().queryChatList(app.getMe().getUsername());
		for (ChatListMessage chatListMessage : users) {
			switch (chatListMessage.getType()) {
			case DBConstants.MSG_TYPE_TXT:

				break;
			case DBConstants.MSG_TYPE_LOCATION:
				chatListMessage.setContent("[位置]");
				break;
			case DBConstants.MSG_TYPE_FILE:
				chatListMessage.setContent("[附件]");
				break;
			default:
				break;
			}
		}
		return users;
	}
}
