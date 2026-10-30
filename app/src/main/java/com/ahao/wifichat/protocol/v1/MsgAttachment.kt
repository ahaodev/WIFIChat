package com.ahao.wifichat.protocol.v1;

import com.google.gson.Gson;
import com.ahao.wifichat.protocol.Fields;


import android.os.Parcel;
import android.os.Parcelable;

public class MsgAttachment extends Fields implements Parcelable {

	private String uuid;
	private int type;
	private String name;
	private long size;

	public MsgAttachment() {
	}

	public MsgAttachment(Parcel source) {
		uuid = source.readString();
		type = source.readInt();
		name = source.readString();
		size = source.readLong();
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String toJson() {

		return new Gson().toJson(this);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uuid);
		dest.writeInt(type);
		dest.writeString(name);
		dest.writeLong(size);
	}

	public static final Creator<MsgAttachment> CREATOR = new Creator<MsgAttachment>() {

		@Override
		public MsgAttachment createFromParcel(Parcel source) {
			return new MsgAttachment(source);
		}

		@Override
		public MsgAttachment[] newArray(int size) {
			return new MsgAttachment[size];
		}
	};
}
