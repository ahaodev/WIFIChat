package com.ahao.wifichat.protocol.v1;

import com.google.gson.Gson;
import com.ahao.wifichat.protocol.Fields;

import android.os.Parcel;
import android.os.Parcelable;

public class MsgLocation extends Fields implements Parcelable {

	private String name;
	private double lon;
	private double lat;

	public String getName() {
		return name;
	}

	public MsgLocation() {
	}

	public MsgLocation(Parcel source) {
		name = source.readString();
		lon = source.readLong();
		lat = source.readLong();
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
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
		dest.writeString(name);
		dest.writeDouble(lon);
		dest.writeDouble(lat);
	}
	public static final Creator<MsgLocation> CREATOR = new Creator<MsgLocation>() {

		@Override
		public MsgLocation createFromParcel(Parcel source) {
			return new MsgLocation(source);
		}

		@Override
		public MsgLocation[] newArray(int size) {
			return new MsgLocation[size];
		}
	};
}
