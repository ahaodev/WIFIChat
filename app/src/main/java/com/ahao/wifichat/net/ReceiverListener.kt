package com.ahao.wifichat.net;

public interface ReceiverListener {
	/**
	 * 收消息回调
	 * 
	 * @param message
	 * @param ipAddress
	 */
	void messageArrived(String message, String ipAddress, int port);
}
