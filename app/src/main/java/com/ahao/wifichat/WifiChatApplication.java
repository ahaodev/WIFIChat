package com.ahao.wifichat;

import java.io.File;
import java.util.HashMap;

import org.litepal.LitePal;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.view.Display;
import android.view.WindowManager;

import com.ahao.wifichat.net.file.FileState;

public class WifiChatApplication extends Application {
	static WifiChatApplication app;
	private Display display;
	public static HashMap<String, FileState> sendFileStates;
	public static HashMap<String, FileState> recieveFileStates;

	@Override
	public void onCreate() {
		WiFiChat.init(this);
		LitePal.initialize(this);
		super.onCreate();
		app = this;
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		sendFileStates = new HashMap<>();
		recieveFileStates = new HashMap<>();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		WiFiChat.instance().stopService();//停止后台服务
	}

	public static WifiChatApplication getInstance() {
		return app;
	}

	public String getAppPath() {
		File appPath = null;
		appPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());// 外置
		if (appPath.exists()) {
			File appFiles = new File(appPath, "Wifi-Chat");
			if (!appFiles.exists()) {
				appFiles.mkdirs();
			}
			return appFiles.getAbsolutePath();
		}
		appPath = new File(Environment.getDataDirectory().getAbsolutePath());// 内置
		if (appPath.exists()) {
			File appFiles = new File(appPath, "Wifi-Chat");
			if (!appFiles.exists()) {
				appFiles.mkdirs();
			}
			return appFiles.getAbsolutePath();
		}
		return "";
	}

	public int getQuarterWidth() {
		return display.getWidth() / 4;
	}

	AppConfig config = AppConfig.getInstance();

	public AppConfig appConfig() {
		return config;
	}
	
}
