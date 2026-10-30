package com.ahao.wifichat.db;

public interface DBConstants {
	/**
	 * 消息类型
	 */
	final int MSG_TYPE_TXT = 1;
	final int MSG_TYPE_LOCATION = 2;
	final int MSG_TYPE_FILE = 3;
	/**
	 * 消息方向
	 */
	final int DIR_TYPE_I = 1;// 当前用户发送
	final int DIR_TYPE_Y = 2;// 接收到的消息
	/**
	 * 文件类型
	 */
	final int FILE_TYPE_IMG = 1;// 图片
	final int FILE_TYPE_VIDEO = 2;// 视频
	final int FILE_TYPE_AUDIO = 3;// 音频
	final int FILE_TYPE = 4;// file
	/**
	 * 文件下载状态
	 */
	final int DOWNLOAD_OK = 1;// 下载完成
	final int DOWNLOAD_ERR = 0;// 下载未完成
	/**
	 * 发送情况
	 */
	final int SEND_STATUS_ING = 0;// 正在发送
	final int SEND_STATUS_ERR = 1;// 发送错误
	final int SEND_STATUS_OK = 2;// 对方收到
	/**
	 * 未读已读
	 */
	final int READ_N = 0;
	final int READ_Y = 1;
}
