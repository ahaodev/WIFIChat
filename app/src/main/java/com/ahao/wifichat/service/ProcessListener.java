package com.ahao.wifichat.service;


import com.ahao.wifichat.service.parcelable.OnlineUserInfo;
import com.ahao.wifichat.service.parcelable.UserLocation;
import com.ahao.wifichat.service.parcelable.UserMessage;

/**
* Created by xiang.shen on 2017年5月8日.
*
*/
public interface ProcessListener {
	
	/**
	 * 上下线回调
	 * @param userInfo
	 */
	public void onOnlineUserUpdate(OnlineUserInfo userInfo);
	/**
	 * 新消息（文本消息等）
	 * @param msg
	 */
	public void onNewMessage(UserMessage msg);
	
	interface LocationListener{
		void onLocationMessage(UserLocation message);
	}
}
