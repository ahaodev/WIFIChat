package com.ahao.wifichat.protocol.v1;

import com.google.gson.Gson;


import android.os.Parcel;
import android.os.Parcelable;

/** 
* Created by xiang.shen on 2017年5月8日.
*
*/
public class MessageInfo<T> implements Parcelable {
	
	private int msg_id;
	private T content;

	public MessageInfo(){

	}
	protected MessageInfo(Parcel in) {
		msg_id = in.readInt();
	}

	public static final Creator<MessageInfo> CREATOR = new Creator<MessageInfo>() {
		@Override
		public MessageInfo createFromParcel(Parcel in) {
			return new MessageInfo(in);
		}

		@Override
		public MessageInfo[] newArray(int size) {
			return new MessageInfo[size];
		}
	};

	public int getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(int msg_id) {
		this.msg_id = msg_id;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	public String toJson() {
		
		return new Gson().toJson(this);
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		dest.writeInt(msg_id);
	}
	

}
