package com.ahao.wifichat.service;


import com.ahao.wifichat.protocol.codec.Pack;

/**
 * 负责处理接收到的数据
 */
public interface PacketListener {

	/**
	 * Process the next packet sent to this packet listener.
	 * <p>
	 * 
	 * A single thread is responsible for invoking all listeners, so it's very
	 * important that implementations of this method not block for any extended
	 * period of time.
	 * 
	 * @param packet
	 *            the packet to process.
	 */
	public void processPacket(Pack packet);

}