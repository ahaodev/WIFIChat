package com.ahao.wifichat.service.filter;


import com.ahao.wifichat.protocol.codec.Pack;

/**
* Created by xiang.shen on 2017年5月5日.
*
*/ 
public class PacketReceiveFilter implements PacketFilter {

	private int cmd;

	public PacketReceiveFilter(int cmd) {
		this.cmd = cmd;
	}

	@Override
	public boolean accept(Pack packet) {
		boolean ret = false;
		if (null != packet && packet.getCommand() == cmd)
			ret = true;
		return ret;
	}
}
