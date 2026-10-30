package com.ahao.wifichat.service;


import com.ahao.wifichat.WiFiChat;
import com.ahao.wifichat.db.ChatDB;
import com.ahao.wifichat.db.DBConstants;
import com.ahao.wifichat.entity.Userinfo;
import com.ahao.wifichat.net.DataConnection;
import com.ahao.wifichat.net.file.FileState;
import com.ahao.wifichat.protocol.codec.Pack;
import com.ahao.wifichat.protocol.v1.MsgAttachment;
import com.ahao.wifichat.protocol.v1.MsgLocation;
import com.ahao.wifichat.service.parcelable.OnlineUserInfo;

public class ChatManager {

	private final DataConnection mDataConnection;
	private final Userinfo mSelfUserInfo;

	public ChatManager(DataConnection conn) {
		mDataConnection = conn;

		mSelfUserInfo = WiFiChat.instance().getMe();
	}

	/**
	 * 发送文本消息
	 * @param destUser
	 * @param content
	 */
	public boolean sendText(OnlineUserInfo destUser, String content) {
		int msg_id = ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_I,content, DBConstants.MSG_TYPE_TXT,
				System.currentTimeMillis(), mSelfUserInfo.getUsername(), destUser);
		// 消息打包然后发送
		Pack pack = PackCommand.getInstance().packSendText(mSelfUserInfo.getUsername(), msg_id, content);
		return mDataConnection.sendData(destUser, pack);
	}

	/**
	 * 广播位置
	 *
	 * @param lon
	 * @param lat
	 * @param name
	 */
	public void broadcastLocation(double lon, double lat, String name) {
		Pack pack = PackCommand.getInstance().packBRLocation(mSelfUserInfo.getUsername(), lon, lat, name);
		mDataConnection.sendData(pack);
	}

	/**
	 * 发送位置
	 * @param destUser
	 * @param lon
	 * @param lat
	 * @param name
	 */
	public void sendLocation(OnlineUserInfo destUser, double lon, double lat, String name) {

		MsgLocation loc = new MsgLocation();
		loc.setLon(lon);
		loc.setLat(lat);
		loc.setName(name);

		int msg_id = ChatDB.getInstance().insertMessage(DBConstants.DIR_TYPE_I,loc.toJson(), DBConstants.MSG_TYPE_LOCATION,
				System.currentTimeMillis(), mSelfUserInfo.getUsername(), destUser);

		// 消息打包然后发送
		Pack pack = PackCommand.getInstance().packSendLocation(mSelfUserInfo.getUsername(), msg_id, lon, lat, name);
		mDataConnection.sendData(destUser, pack);

	}

	/**
	 * 上线
	 */
	public void sendEntry() {
		Pack pack = PackCommand.getInstance().packSendEntry(mSelfUserInfo.getUsername(), mSelfUserInfo.getName(),
				mSelfUserInfo.getIpAdderss());
		mDataConnection.sendData(pack);
	}

	/**
	 * 下线
	 */
	public void sendOutLine() {
		Pack pack = PackCommand.getInstance().packSendExit(mSelfUserInfo.getUsername());
		mDataConnection.sendData(pack);
	}

	/***
	 * 附件通知消息
	 *
	 */
	public void sendFileUdp(OnlineUserInfo dstuser, String uuid, String filePath, int status, FileState file, int msg_id) {
		MsgAttachment att = new MsgAttachment();
		att.setUuid(uuid);
		att.setType(file.type);
		att.setName(file.fileName);
		att.setSize(file.fileSize);

		Pack pack = PackCommand.getInstance().packSendAttachment(mSelfUserInfo.getUsername(), msg_id, uuid, file.type,
				file.fileName, file.fileSize);
		mDataConnection.sendData(dstuser, pack);
	}
}
