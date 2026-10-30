package com.ahao.wifichat.net;


import com.ahao.wifichat.service.parcelable.OnlineUserInfo;

import java.util.concurrent.Callable;

public class SendDataUDP implements Callable<Boolean> {
	
	private final String privMsg;
	private final OnlineUserInfo destUser;
	private final NetworkService mNetworkService;
	
	public SendDataUDP(final NetworkService networkService,final OnlineUserInfo user, final String privMsg) {
		// TODO Auto-generated constructor stub
		this.privMsg = privMsg;
		this.destUser = user;
		this.mNetworkService = networkService;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		boolean sent = mNetworkService.sendUDPMsg(privMsg, destUser.getIpAdderss(), destUser.getPort());
		return sent;
	}

}
