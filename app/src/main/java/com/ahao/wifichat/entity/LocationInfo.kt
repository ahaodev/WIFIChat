package com.ahao.wifichat.entity;

import com.google.gson.Gson;
/**
 * 位置信息转json
 * @author hao.ruan
 *
 */
public class LocationInfo {
	String lon;
	String lat;

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
