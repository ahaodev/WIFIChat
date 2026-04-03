package com.ahao.wifichat.ui.contacts;

import java.util.ArrayList;
import java.util.List;



import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ahao.wifichat.R;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.service.P2PService;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.ui.BaseFragment;
import com.ahao.wifichat.ui.MainActivity;
import com.ahao.wifichat.ui.adapter.CommonAdapter;
import com.ahao.wifichat.ui.adapter.ViewHolder;
import com.ahao.wifichat.ui.chat.ChatActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ContactsFragment extends BaseFragment {
	public static final String TAG = "ContactsFragment";
	public static final int USER_ADD = 1;
	public static final int USER_REM = 0;

	private View v;
	private ListView mListView;
	private CommonAdapter<OnlineUserInfo> adapter;
	private List<OnlineUserInfo> contacts = new ArrayList<OnlineUserInfo>();
	private WiFiChat app;
	private P2PService.P2PServiceBindle service;
	private IntentFilter intentFilter;
	private MainActivity main;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.p2p_fragment_chatlist, null);
		app = WiFiChat.instance();
		service = app.getBinder();
		init();
		intentFilter = new IntentFilter(TAG);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		main = (MainActivity) getActivity();
		main.getTitleView().setTitle(getTag());
		main.getTitleView().setRightVisibility(View.INVISIBLE);
		if (null != service && !service.getUsers().isEmpty()) {
			adapter.notifyDataSetChanged(service.getUsers());
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void init() {
		mListView = (ListView) v.findViewById(R.id.chat_fragment_listview);
		adapter = new CommonAdapter<OnlineUserInfo>(getActivity(), contacts, R.layout.p2p_item_devices) {

			@Override
			public void convert(ViewHolder helper, final OnlineUserInfo item) {
				helper.setImageResource(R.id.icon, R.drawable.default_person);
				helper.setText(R.id.device_name, item.getName());
				helper.setText(R.id.device_details, item.getUserName());
			}
		};
		mListView.setAdapter(adapter);
		if (null != service && !service.getUsers().isEmpty()) {

			adapter.notifyDataSetChanged(service.getUsers());
		}
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OnlineUserInfo item = adapter.getItem(position);
				item.setOnline(1);
				startActivity(ChatActivity.class, ChatActivity.USER, item);
			}
		});
	}

	public void notifyData(OnlineUserInfo userinfo) {
		Observable.just(userinfo).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<OnlineUserInfo>() {

			@Override
			public void call(OnlineUserInfo user) {
				if (null != service) {
					adapter.notifyDataSetChanged(service.getUsers());
				}
			}
		});
	}
}
