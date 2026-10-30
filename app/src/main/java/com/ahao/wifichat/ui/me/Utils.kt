package com.ahao.wifichat.ui.me;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class Utils {

	public static final String TAG = "Utils";

	public static boolean isNetworkAvailable(Context var0) {
		ConnectivityManager cm = (ConnectivityManager) var0
				.getSystemService("connectivity");
		if (cm == null) {
			Log.w(TAG, "couldn\'t get connectivity manager");
		} else {
			NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
			if (networkInfo != null) {
				for (int i = 0; i < networkInfo.length; ++i) {
					if (networkInfo[i].getState() == State.CONNECTED) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static String getUniqueId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm != null) {
			String imei = tm.getDeviceId();
			if (imei != null)
				return imei;
		}
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null) {
				String mac = info.getMacAddress();
				if (!TextUtils.isEmpty(mac)) {
					return mac.replaceAll(":", "");
				}
			}
		}
		return "";
	}


	public static String MD5(String inputString) {
		StringBuffer md5String = new StringBuffer("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(inputString.getBytes());
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					md5String.append("0");
				}
				md5String.append(Integer.toHexString(i));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5String.toString().toUpperCase();
	}
}
