package com.ahao.wifichat.service.parcelable;


import android.os.Parcel;
import android.os.Parcelable;

import com.ahao.wifichat.protocol.constant.EnumMsgType;

/** 
* Created by xiang.shen on 2017年5月8日.
*
*/
public class UserMessage implements Parcelable{
	
	private long id;
	private EnumMsgType type;
	private String content;
	private long msg_time;
	private String sendUsername;

	public UserMessage() {
	}

	public UserMessage(Parcel in) {
		id = in.readLong();
		content = in.readString();
		msg_time = in.readLong();
		sendUsername = in.readString();
	}

	public static final Creator<UserMessage> CREATOR = new Creator<UserMessage>() {
		@Override
		public UserMessage createFromParcel(Parcel in) {
			return new UserMessage(in);
		}

		@Override
		public UserMessage[] newArray(int size) {
			return new UserMessage[size];
		}
	};

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public EnumMsgType getType() {
		return type;
	}

	public void setType(EnumMsgType type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getMsg_time() {
		return msg_time;
	}

	public void setMsg_time(long msg_time) {
		this.msg_time = msg_time;
	}
	
	public String getSendUsername() {
		return sendUsername;
	}

	public void setSendUsername(String sendUsername) {
		this.sendUsername = sendUsername;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		dest.writeLong(id);
		dest.writeString(content);
		dest.writeLong(msg_time);
		dest.writeString(sendUsername);
	}

}
