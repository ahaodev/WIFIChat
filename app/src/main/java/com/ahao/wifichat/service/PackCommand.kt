package com.ahao.wifichat.service;


import com.ahao.wifichat.protocol.Fields;
import com.ahao.wifichat.protocol.P2pCmd;
import com.ahao.wifichat.protocol.codec.Pack;
import com.ahao.wifichat.protocol.v1.Entry;
import com.ahao.wifichat.protocol.v1.MessageInfo;
import com.ahao.wifichat.protocol.v1.MsgAttachment;
import com.ahao.wifichat.protocol.v1.MsgLocation;
import com.ahao.wifichat.protocol.v1.MsgText;
import com.ahao.wifichat.service.parcelable.ReplyMessage;

/**
 * 打包数据内容
 * 
 * @author xiang.shen
 */
public class PackCommand extends IBasePackProtocol {

	private static PackCommand sInstance = null;

	public static PackCommand getInstance() {
		if (sInstance == null)
			sInstance = new PackCommand();
		return sInstance;
	}

	/**
	 * 打包上线通知
	 * 
	 * @param username
	 * @return pack
	 */
	public Pack packSendEntry(String username, String name, String ipAdderss) {
		Entry entry = new Entry();
		entry.setName(name);
		entry.setIpAdderss(ipAdderss);
		return super.createPack(username, P2pCmd.CMD_BR_ENTRY, entry.toJson());
	}

	/**
	 * 打包回复上线通知
	 * 
	 * @param username
	 * @return Pack
	 */
	public Pack packReplyEntry(String username, String name, String ipAdderss) {
		Entry entry = new Entry();
		entry.setName(name);
		entry.setIpAdderss(ipAdderss);
		return super.createPack(username, P2pCmd.CMD_ANS_ENTRY, entry.toJson());
	}

	/**
	 * 打包下线通知
	 * 
	 * @param username
	 * @return
	 */
	public Pack packSendExit(String username) {
		return super.createPack(username, P2pCmd.CMD_BR_EXIT, "");
	}

	/**
	 * 打包消息
	 * 
	 * @param username
	 * @param cmd
	 * @param fields
	 * @return
	 */
	public Pack packMessage(String username, int cmd, int msgid, Fields fields) {
		MessageInfo<Fields> msg = new MessageInfo<Fields>();
		msg.setMsg_id(msgid);
		msg.setContent(fields);
		return super.createPack(username, cmd, msg.toJson());
	}

	/**
	 * 打包文本消息
	 * 
	 * @param username
	 * @return
	 */
	public Pack packSendText(String username, int msgid, String content) {
		MsgText msg = new MsgText();
		msg.setText(content);
		return packMessage(username, P2pCmd.CMD_SEND_TEXT, msgid, msg);
	}

	/**
	 * 打包位置消息
	 * 
	 * @param username
	 * @param lon
	 * @param lat
	 * @param name
	 * @return
	 */
	public Pack packSendLocation(String username, int msgid, double lon, double lat, String name) {
		MsgLocation loc = new MsgLocation();
		loc.setLon(lon);
		loc.setLat(lat);
		loc.setName(name);
		return packMessage(username, P2pCmd.CMD_SEND_LOC, msgid, loc);
	}

	/**
	 * 打包广播位置消息
	 * 
	 * @param username 本人的
	 * @param lon
	 * @param lat
	 * @param name 地名
	 * @return
	 */
	public Pack packBRLocation(String username, double lon, double lat, String name) {
		MsgLocation loc = new MsgLocation();
		loc.setLon(lon);
		loc.setLat(lat);
		loc.setName(name);
		return super.createPack(username, P2pCmd.CMD_BR_LOC, loc.toJson());
	}

	/**
	 * 打包附件消息
	 * 
	 * @param username
	 * @param uuid
	 * @param type
	 * @param name
	 * @param size
	 * @return
	 */
	public Pack packSendAttachment(String username, int msgid, String uuid, int type, String name, long size) {
		MsgAttachment attac = new MsgAttachment();
		attac.setUuid(uuid);
		attac.setType(type);
		attac.setName(name);
		attac.setSize(size);
		return packMessage(username, P2pCmd.CMD_FILE_NOTICE, msgid, attac);
	}

	/**
	 * 打包回复消息
	 * 
	 * @param username
	 * @param cmd
	 * @param sn
	 * @param msg_id
	 * @return
	 */
	public Pack packReplyMessage(String username, int cmd, int sn, int msg_id) {
		ReplyMessage msg = new ReplyMessage();
		msg.setCmd(cmd);
		msg.setPacket_no(sn);
		msg.setMsg_id(msg_id);
		return super.createPack(username, P2pCmd.CMD_RECV_MSG, msg.toJson());
	}
}
