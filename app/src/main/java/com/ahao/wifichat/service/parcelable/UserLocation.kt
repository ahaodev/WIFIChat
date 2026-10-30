package com.ahao.wifichat.service.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class UserLocation implements Parcelable{
	
	private String username;
	private String name;
	private double lon;
	private double lat;

	public UserLocation() {
	}

	protected UserLocation(Parcel in) {
		username = in.readString();
		name = in.readString();
		lon = in.readDouble();
		lat = in.readDouble();
	}

	public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
		@Override
		public UserLocation createFromParcel(Parcel in) {
			return new UserLocation(in);
		}

		@Override
		public UserLocation[] newArray(int size) {
			return new UserLocation[size];
		}
	};

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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		dest.writeString(username);
		dest.writeString(name);
		dest.writeDouble(lon);
		dest.writeDouble(lat);
	}

}
