package com.ahao.wifichat.protocol.v1;

import com.google.gson.Gson;
import com.ahao.wifichat.protocol.Fields;


import android.os.Parcel;
import android.os.Parcelable;

/** 
* Created by xiang.shen on 2017年5月8日.
*
*/
public class MsgText extends Fields implements Parcelable {
	
	private String text;

	public MsgText(){

	}
	protected MsgText(Parcel in) {
		text = in.readString();
	}

	public static final Creator<MsgText> CREATOR = new Creator<MsgText>() {
		@Override
		public MsgText createFromParcel(Parcel in) {
			return new MsgText(in);
		}

		@Override
		public MsgText[] newArray(int size) {
			return new MsgText[size];
		}
	};

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

		dest.writeString(text);
	}
	

}
