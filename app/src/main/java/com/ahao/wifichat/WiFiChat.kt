package com.ahao.wifichat;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;


import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.net.Constants;

import com.ahao.wifichat.net.file.TcpService;
import com.ahao.wifichat.service.P2PService;
import com.ahao.wifichat.utlis.NetworkUtils;

import java.io.File;

public class WiFiChat extends ContextWrapper {
	private static WiFiChat _instance = null;
	P2PService.P2PServiceBindle mBinder=null;
	ActivityManager activityManager = new ActivityManager();
	private Userinfo me = new Userinfo();
	public static WiFiChat instance() {
		return _instance;
	}

	public WiFiChat(Context base) {
		super(base);
		me.setIpAdderss(NetworkUtils.getLocalIpAddress());
		me.setPort(Constants.NETWORK_UDP_PORT);
		startService();
		initNotifcation();
	}

	private void initNotifcation() {
	
	}

	public static void init(Application app) {
		synchronized (WiFiChat.class) {
			_instance = new WiFiChat(app);
		}
	}

	public ActivityManager getActivityManager() {
		return activityManager;
	}
	Intent intentService = new Intent(this, P2PService.class);
	public void startService() {
		startService(intentService);
		bindService(intentService, conn, Context.BIND_AUTO_CREATE);
	}
	public void stopService(){
		stopService(intentService);
		unbindService(conn);
	}
	public void exit() {
		unbindService(conn);
		System.exit(0);
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (P2PService.P2PServiceBindle) service;
			initTCPService();
//			mBinder.getChatManager().sendEntry();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
//			mBinder.getChatManager().sendOutLine();
		}

	};
	 TcpService tcpService;
	/**
	 * 文件接受线程
	 */
	public void initTCPService() {
		String foloder = WifiChatApplication.getInstance().getAppPath();
		File savedir = new File(foloder);
		if (!savedir.exists()) {
			savedir.mkdirs();
		}
		tcpService = TcpService.getInstance(this);
		TcpService.setHandler(new Handler());
		tcpService.setSavePath(savedir.getAbsolutePath());
		tcpService.startReceive();
	}
	public P2PService.P2PServiceBindle getBinder() {
		return mBinder;
	}

	Userinfo location_user = new Userinfo();
	NetworkUtils network = new NetworkUtils();
	// 获取自己的信息
	public Userinfo getMe() {
		return me;
	}

}
