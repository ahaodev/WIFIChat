package com.ahao.wifichat.net;

import java.util.concurrent.Callable;

public class SendDataMulticast implements Callable<Boolean> {

	private final String privMsg;
	private final NetworkService mNetworkService;

	public SendDataMulticast(final NetworkService networkService, final String privMsg) {
		// TODO Auto-generated constructor stub
		this.privMsg = privMsg;
		this.mNetworkService = networkService;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
//		boolean sent = mNetworkService.sendMulticastMsg(privMsg);
		return false;
	}

}
