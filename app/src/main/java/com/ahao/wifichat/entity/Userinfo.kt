package com.ahao.wifichat.entity;

import com.google.gson.Gson;
import com.ahao.wifichat.net.Constants;
import com.ahao.wifichat.utlis.NetworkUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户信息
 * 
 * @author hao.ruan
 *
 */
public class Userinfo implements Parcelable {

	String ipAdderss = NetworkUtils.getLocalIpAddress();
	int port = Constants.NETWORK_PRIVCHAT_PORT;

	String username;
	String name;

	public Userinfo() {

	}

	public Userinfo(String userName, String name, String ip, int port) {
		this.username = userName;
		this.name = name;
		this.ipAdderss = ip;
		this.port = port;
	}

	public Userinfo(Parcel source) {
		username = source.readString();
		ipAdderss = source.readString();
		port = source.readInt();
		name = source.readString();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAdderss() {
		return ipAdderss;
	}

	public void setIpAdderss(String ipAdderss) {
		this.ipAdderss = ipAdderss;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(username);
		dest.writeString(ipAdderss);
		dest.writeInt(port);
		dest.writeString(name);
	}

	public static final Creator<Userinfo> CREATOR = new Creator<Userinfo>() {

		@Override
		public Userinfo createFromParcel(Parcel source) {
			return new Userinfo(source);
		}

		@Override
		public Userinfo[] newArray(int size) {
			return new Userinfo[size];
		}
	};
}
