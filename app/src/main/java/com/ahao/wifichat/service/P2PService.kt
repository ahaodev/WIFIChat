package com.ahao.wifichat.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.db.user_msg;
import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.net.DataConnection;
import com.ahao.wifichat.net.UsersListener;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.service.parcelable.UserLocation;

/**
 * WiFi聊天Service 持有发送和接受
 * 
 * @author hao.ruan
 *
 */
public class P2PService extends Service {
	// 在线人员位置
	public Map<String, UserLocation> mOnlineUsersLocation = new ConcurrentHashMap<String, UserLocation>();
	// 在线列表
	public Map<String, OnlineUserInfo> mOnlineUsers = new ConcurrentHashMap<String, OnlineUserInfo>();
	UsersListener userListener;// 用户上下线回调
	private Map<String, user_msg> conversations = new HashMap<>();// 会话列表

	//会话管理
	private ChatManager mChatManager = null;
	//数据连接管理
	private DataConnection mDataConnection = null;

	Userinfo me;
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final P2PServiceBindle mBinder = new P2PServiceBindle();

	public class P2PServiceBindle extends Binder {
		public List<OnlineUserInfo> getUsers() {
			List<OnlineUserInfo> us = new ArrayList<OnlineUserInfo>();
			for (OnlineUserInfo user : mOnlineUsers.values()) {
				us.add(user);
			}
			return us;
		}

		public OnlineUserInfo getUser(String name) {
			return P2PService.this.getOnlineUser(name);
		}
		public List<UserLocation> getUsersLocation() {
			List<UserLocation> locations = new ArrayList<UserLocation>();
			for (UserLocation location : mOnlineUsersLocation.values()) {
				locations.add(location);
			}
			return locations;
		}
		public void OnlineUsersClear(){
			mOnlineUsers.clear();
			mOnlineUsersLocation.clear();
		}
//		public Map<String, ChatMessage> getConversationsMap() {
//			return conversations;
//		}

//		public List<ChatMessage> getConversationsList() {
//			List<ChatMessage> us = new ArrayList<ChatMessage>();
//			for (ChatMessage user : conversations.values()) {
//				us.add(user);
//			}
//			return us;
//		}
		
		// 获取数据连接
		public DataConnection getDataConnection() {
			return mDataConnection;
		}
		
		public ChatManager getChatManager() {
			return mChatManager;
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		me= WiFiChat.instance().getMe();
		// 设置个人信息
		mDataConnection = new DataConnection(this);
		mChatManager = new ChatManager(mDataConnection);
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mDataConnection.onDestroy();
	}

	public synchronized void addOnlineUser(OnlineUserInfo user) {
		String frist_IP=user.getIpAdderss();//上一个用户IP
		for (Map.Entry<String, OnlineUserInfo> usermap : mOnlineUsers.entrySet()) {
			if (frist_IP.equals(usermap.getValue().getIpAdderss())&&!user.getUserName().equals(usermap.getValue().getUserName())) {
				mOnlineUsers.remove(usermap.getKey());//剔除同一个机器的上一个用户，避免下线失败
			}
		}
		if (!mOnlineUsers.containsKey(user.getUserName())) {//避免重复上线
			mOnlineUsers.put(user.getUserName(), user);
		}
	}
	
	public void addOnlineUserLocation(UserLocation location) {
		mOnlineUsersLocation.put(location.getUsername(), location);
	}
	
	public OnlineUserInfo getOnlineUser(String userName) {
		OnlineUserInfo user = null;
		if (mOnlineUsers.containsKey(userName)) {
			user = mOnlineUsers.get(userName);
		}
		return user;
	}
	public UserLocation getOnlineUserLocation(String userName) {
		UserLocation location = null;
		if (mOnlineUsersLocation.containsKey(userName)) {
			location = mOnlineUsersLocation.get(userName);
		}
		return location;
	}
	public void delOnlineUser(String userName) {
		mOnlineUsers.remove(userName);
		mOnlineUsersLocation.remove(userName);
	}
	
	public boolean isOnlineUser(String userName) {
		return mOnlineUsers.containsKey(userName);
	}

	public Map<String, OnlineUserInfo> getOnlineUsers() {
		return mOnlineUsers;
	}

}
