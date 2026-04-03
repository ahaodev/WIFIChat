package com.ahao.wifichat.service;


import com.ahao.wifichat.protocol.P2pVersion;
import com.ahao.wifichat.protocol.SerialNumberUtils;
import com.ahao.wifichat.protocol.codec.Pack;

public abstract class IBasePackProtocol {
	
	protected Pack createPack(final String strSendUserName, final int nCommand, final String strContent) {
		Pack pack = new Pack();
		pack.setVersion(P2pVersion.ONE);
		pack.setSn(SerialNumberUtils.getNewNumIdx());
		pack.setSendName(strSendUserName);
		pack.setCommand(nCommand);
		pack.setContent(strContent);
		return pack;
	}
}
