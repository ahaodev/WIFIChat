package com.ahao.wifichat.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTabHost;

import com.ahao.wifichat.R;
import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.service.ProcessListener;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.service.parcelable.UserMessage;
import com.ahao.wifichat.ui.chatlist.ChatListFragment;
import com.ahao.wifichat.ui.contacts.ContactsFragment;
import com.ahao.wifichat.ui.me.PersonalCenterFragment;
import com.ahao.wifichat.widget.TitleView;

public class MainActivity extends FragmentActivity {

	private LayoutInflater layoutInflater;
	private FragmentTabHost mTabHost;
	private Class<?> fragmentArray[] = { ChatListFragment.class, ContactsFragment.class,
			PersonalCenterFragment.class };
	private int mTextviewArray[] = { R.string.chat, R.string.contacts, R.string.me };

	private int mImageViewArray[] = { R.drawable.selector_wechat, R.drawable.selector_contacts,
			R.drawable.selector_me };
	private Intent intent;
	private Userinfo user;
	private WiFiChat app;
	private TitleView titleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 垂直显示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		intent = getIntent();
		app = WiFiChat.instance();
		setContentView(R.layout.p2p_activity_main);
		initView();
		app.getActivityManager().pushActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		app = WiFiChat.instance();
		updateUnreadCount();
		
		if (null==app.getBinder()) {
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					app.getBinder().getDataConnection().addProcessListener(listener);
				}
			}, 1000);
		}else {
			app.getBinder().getDataConnection().addProcessListener(listener);
		}
		
	}


	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.getBinder().getDataConnection().removeProcessListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:

			break;
		case R.id.action_devicelist:
			WiFiChat.instance().getBinder().getChatManager().sendEntry();
			break;
		case R.id.test_singout:
			WiFiChat.instance().getBinder().getChatManager().sendOutLine();
			break;
		case R.id.test_255:
			send255Test();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initView() {
		titleView = (TitleView) findViewById(R.id.title);
		titleView.setBackVisibility(View.INVISIBLE);

		layoutInflater = LayoutInflater.from(this);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(getString(mTextviewArray[i])).setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
		}
	}

	@SuppressLint("InflateParams")
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.p2p_tab_item_view_main, null);

		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);
		Drawable top = getResources().getDrawable(mImageViewArray[index]);
		textView.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);

		return view;
	}

	private ProcessListener listener = new ProcessListener() {

		@Override
		public void onOnlineUserUpdate(OnlineUserInfo userInfo) {
			notityContacts(userInfo);
			updateUnreadCount();
		}

		@Override
		public void onNewMessage(UserMessage msg) {
			updateUnreadCount();

		}
	};

	private void updateUnreadCount() {
		int count = ChatDB.getInstance().findMessageReadAll();
		View v = mTabHost.getTabWidget().getChildTabViewAt(0);
		if (v != null) {
			TextView tv = (TextView) v.findViewById(R.id.tvTip);

			if (count > 0) {
				tv.setVisibility(View.VISIBLE);
				tv.setText(String.valueOf(count));
			} else {
				tv.setVisibility(View.GONE);
			}
		}
		notityChatList();
	}

	private void notityChatList() {
		ChatListFragment chatList = (ChatListFragment) getSupportFragmentManager()
				.findFragmentByTag(getResources().getString(mTextviewArray[0]));
		if (null == chatList) {
			return;
		}
		chatList.notifyDate();
	}

	private void notityContacts(OnlineUserInfo userinfo) {
		ContactsFragment chatList = (ContactsFragment) getSupportFragmentManager()
				.findFragmentByTag(getResources().getString(mTextviewArray[1]));
		if (null == chatList) {
			return;
		}
		chatList.notifyData(userinfo);
	}

	/**
	 * 测试
	 */
	public void sendSingInTest() {
		// final SendMessageProxy send = SendMessageProxy.getInstances();
		// Observable.just("上线测试").subscribeOn(Schedulers.newThread()).subscribe(new
		// Action1<String>() {
		//
		// @Override
		// public void call(String v) {
		// Log.e("TAG", "发送情况:" + send.sendSignIn(MainActivity.this));
		// }
		// });
	}

	private void sendSingOutTest() {
		// final SendMessageProxy send = SendMessageProxy.getInstances();
		// Observable.just("下线测试").subscribeOn(Schedulers.newThread()).subscribe(new
		// Action1<String>() {
		//
		// @Override
		// public void call(String v) {
		// Log.e("TAG", "发送情况:" + send.sendSingnOut());
		// }
		// });
	}

	private void send255Test() {
		// final SendMessageProxy send = SendMessageProxy.getInstances();
		// Observable.just("下线测试").subscribeOn(Schedulers.newThread()).subscribe(new
		// Action1<String>() {
		//
		// @Override
		// public void call(String v) {
		// Log.e("TAG", "发送情况:" + send.sendText("255.255.255.255",
		// Constants.NETWORK_TEMP_PORT, "255"));
		// }
		// });
	}
	public TitleView getTitleView(){
		return titleView;
	}
}
