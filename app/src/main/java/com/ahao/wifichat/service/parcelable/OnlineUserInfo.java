package com.ahao.wifichat.service.parcelable;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xiang.shen on 2017年5月5日.
 *
 */
public class OnlineUserInfo implements Parcelable {

	private String userName;
	private String name;
	private String ipAdderss;
	private int port;
	
	private int online=1;//0 不在线,1在线  ，当不在线时 ip和 prot 为空
	public OnlineUserInfo() {
	}

	public OnlineUserInfo(Parcel source) {
		userName = source.readString();  
		name = source.readString();
		ipAdderss = source.readString();
		port = source.readInt();
		online=source.readInt();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	
	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userName);
		dest.writeString(name);
		dest.writeString(ipAdderss);
		dest.writeInt(port);
		dest.writeInt(online);
	}

	public static final Creator<OnlineUserInfo> CREATOR = new Creator<OnlineUserInfo>() {

		@Override
		public OnlineUserInfo createFromParcel(Parcel source) {
			return new OnlineUserInfo(source);
		}

		@Override
		public OnlineUserInfo[] newArray(int size) {
			return new OnlineUserInfo[size];
		}
	};
}
