package com.ahao.wifichat.net;
/**
 * 后期可以跟protocol类整合
 * @author hao.ruan
 *
 */
public interface Constants {
	String APP_NAME = "WIFI-Chat";
	
	/**
	 * 广播IP
	 */
	String NETWORK_BROADCAST_IP = "255.255.255.255";
	/**
	 * UDP接收端口
	 */
	int NETWORK_UDP_PORT = 12425;
	
	/**
	 * 用于发送的临时多播地址和接收数据包
	 */
	String NETWORK_TEMP_IP = "224.168.5.250";
	/**
	 * 用于发送和接受的临时组播udp端口
	 */

	int NETWORK_TEMP_PORT = 50056;
	/**
	 * 数据包大小
	 */
	int NETWORK_PACKET_SIZE = 1024;
	/**
	 * 消息编码
	 */
	String MESSAGE_CHARSET = "UTF-8";

	/**
	 * 用于发送和接收的普通udp端口 用于私人聊天的数据包。 这只是起始端口。 如果它已经在使用，端口+1被尝试，等等。
	 */
	int NETWORK_PRIVCHAT_PORT = 40656;
	String APP_VERSION = "0.0.1";
	/**
	 * 用于发送和接收的组播地址 数据包为群聊天。
	 */
	String NETWORK_IP = "224.168.5.200";

	/**
	 * The multicast udp port used for sending and receiving packets for the
	 * main chat.
	 */
	int NETWORK_CHAT_PORT = 40555;
	/**
	 * The folder where the application can save files.
	 */
	String APP_FOLDER = System.getProperty("user.home") + System.getProperty("file.separator") + "."
			+ APP_NAME.toLowerCase() + System.getProperty("file.separator");
	/**
	 * The folder where log files are stored.
	 */
	String APP_LOG_FOLDER = APP_FOLDER + "logs" + System.getProperty("file.separator");

	int TCP_SERVER_RECEIVE_PORT=5557;
	int READ_BUFFER_SIZE = 1024*4;// 文件流缓冲大小
}
