package com.ahao.wifichat.net;

public interface Protocol {
	/**
	 * 上线广播通知
	 */
	int IPMSG_BR_ENTRY = 0x00000001;
	/**
	 * “收到上线通知”确认
	 */
	int IPMSG_ANSENTRY = 0x00000002;
	/**
	 * 下线广播通知
	 */
	int IPMSG_BR_EXIT = 0x00000003;
	/**
	 * 发送消息文本
	 */
	int IPMSG_SENDMSG = 0x00000004;
	/**
	 * 下载文件请求
	 */
	int IPMSG_GETFILEDATA = 0x00000005;
	/**
	 * “收到消息”确认
	 */
	int IPMSG_RECVMSG = 0x00000006;

	int IPMSG_REQUEST_IMAGE_DATA = 0x00000063; // 图片发送请求
	int IPMSG_CONFIRM_IMAGE_DATA = 0x00000064; // 图片接收确认
	int IPMSG_SEND_IMAGE_SUCCESS = 0x00000065; // 图片发送成功
	int IPMSG_REQUEST_VOICE_DATA = 0x00000066; // 录音发送请求
	int IPMSG_CONFIRM_VOICE_DATA = 0x00000067; // 录音接收确认
	int IPMSG_SEND_VOICE_SUCCESS = 0x00000068; // 录音发送成功
	int IPMSG_REQUEST_FILE_DATA = 0x00000069; // 文件发送请求
	int IPMSG_CONFIRM_FILE_DATA = 0x00000070; // 文件接收确认
	/**
	 * 位置消息标识
	 */
	int IPMSG_LOCATIONOPT = 0x00000100;
	/**
	 * 附件消息标识
	 */
	int IPMSG_FILEATTACHOPT = 0x00000200;

}
