package com.ruanhao.wifichat.db;

import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePal;


import android.database.Cursor;
import android.text.TextUtils;

import com.ruanhao.wifichat.WiFiChat;
import com.ruanhao.wifichat.entity.ChatListMessage;
import com.ruanhao.wifichat.net.file.FileState;
import com.ruanhao.wifichat.service.parcelable.OnlineUserInfo;

public class ChatDB {
	
	private static ChatDB sInstance = null;
	
	public static ChatDB getInstance() {
		if (sInstance == null)
			sInstance = new ChatDB();
		return sInstance;
	}
	
	private ChatListMessage _getChatListMessage(Cursor cursor) {
		if (cursor == null)
			return null;

		int idx = -1;
		ChatListMessage msg = new ChatListMessage();
		if ((idx = cursor.getColumnIndex("other_username")) != -1)
			msg.setUsername(cursor.getString(idx));
		if ((idx = cursor.getColumnIndex("other_name")) != -1)
			msg.setName(cursor.getString(idx));
		if ((idx = cursor.getColumnIndex("type")) != -1)
			msg.setType(cursor.getInt(idx));
		if ((idx = cursor.getColumnIndex("content")) != -1)
			msg.setContent(cursor.getString(idx));
		if ((idx = cursor.getColumnIndex("msg_time")) != -1)
			msg.setTime(cursor.getLong(idx));
		return msg;
		
	}
	
	/**
	 * 获取会话列表以时间排序
	 * @param strCurUsername
	 * @return 会话列表
	 */
	public List<ChatListMessage> queryChatList(String strCurUsername) {
		String sql = String.format(
				"select other_username,other_name,type,content,msg_time from user_msg T3 left join msg T4 on T3.msg_id=T4.id where  T3.id in (select id from (select id,max(msg_time) from (select T1.id id,other_username,msg_time from user_msg T1 left join msg T2 on T1.msg_id=T2.id where username = '%s') group by other_username)) order by msg_time desc",
				strCurUsername);
		Cursor cursor = LitePal.findBySQL(sql);
		List<ChatListMessage> list = new ArrayList<ChatListMessage>();

		if (cursor != null && cursor.moveToFirst()) {
			do {
				list.add(_getChatListMessage(cursor));
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 获取消息列表
	 * @param strCurUsername
	 * @param strOtherUsername
	 * @return 消息列表
	 */
	public List<user_msg> queryChatMessage(String strCurUsername, String strOtherUsername) {

		List<user_msg> list = LitePal.where("username = ? and other_username = ?", strCurUsername, strOtherUsername).find(user_msg.class, true);
//		List<ChatMessage> msglist = new ArrayList<ChatMessage>();
		return list;
	}
	
	/**
	 * 插入消息表
	 * @param content
	 * @param type
	 * @param time
	 * @param userList
	 * @param attach
	 * @return 消息id
	 */
	public int insertMsg(String content, int type, long time, List<user_msg> userList, msg_attachment attach) {
		msg msg = new msg();
		msg.setContent(content);
		msg.setType(type);
		msg.setMsg_time(time);
		if (userList != null)
			msg.setUserList(userList);
		if (attach != null)
			msg.setMsg_att(attach);
		msg.save();
		
		return msg.getId();
	}
	
	/**
	 * 插入普通消息
	 * @param dirType 方向
	 * @param content
	 * @param type
	 * @param time
	 * @param username
	 * @param other
	 * @return 消息id
	 */
	public int insertMessage(int dirType, String content, int type, long time, String username, OnlineUserInfo other) {
		user_msg usermsg = new user_msg();
		usermsg.setUsername(username);
		usermsg.setDir_type(dirType);
		usermsg.setOther_name(other.getName());
		usermsg.setOther_username(other.getUserName());
		if(DBConstants.DIR_TYPE_I == dirType)
			usermsg.setStatus(DBConstants.SEND_STATUS_ING);
		else 
			usermsg.setIs_read(DBConstants.READ_N);

		List<user_msg> userList = new ArrayList<user_msg>();
		userList.add(usermsg);
		usermsg.save();

		return insertMsg(content, type, time, userList, null);
	}
	
	/**
	 * 插入附件消息
	 * @param dirType
	 * @param content
	 * @param type
	 * @param time
	 * @param username
	 * @param other
	 * @param file
	 * @param filePath
	 * @param uuid
	 * @param status
	 * @return 消息id
	 */
	public int insertMessage(int dirType, String content, int type, long time, String username, OnlineUserInfo other,
							 FileState file, String filePath, String uuid, int status) {

		user_msg usermsg = new user_msg();
		usermsg.setUsername(username);
		usermsg.setDir_type(dirType);
		usermsg.setOther_name(other.getName());
		usermsg.setOther_username(other.getUserName());

		if (DBConstants.DIR_TYPE_I == dirType)
			usermsg.setStatus(DBConstants.SEND_STATUS_ING);
		else
			usermsg.setIs_read(DBConstants.READ_N);

		List<user_msg> userList = new ArrayList<user_msg>();
		userList.add(usermsg);
		usermsg.save();

		msg_attachment attach = new msg_attachment();
		attach.setFile_uuid(uuid);
		attach.setType(file.type);
		attach.setFilename(file.fileName);
		attach.setSize(file.fileSize);
		attach.setUri(filePath);// 本地存储路径
		attach.setStatus(status);
		attach.save();

		return insertMsg(content, type, time, userList, attach);
	}
	
	/**
	 * 将所有发送状态为'发送中'的消息都修改为'发送失败'
	 * @param username
	 * @param other_name
	 * @param time
	 */
	public void updateSendTimeOut(String username, String other_name, long time) {
		String sql = String.format(
				"update user_msg set status = 1 where exists (select 1 from msg where username = '%s' and other_username = '%s' and msg_time < %d and status = 0 and user_msg.msg_id = msg.id)",
				username, other_name, time);
		Cursor cursor = LitePal.findBySQL(sql);
		int count = 0;
		if (cursor != null && cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
//		return count;
	}

	/**
	 * 获取该消息是否已读
	 * 
	 * @return
	 */
	public int findMessageRead(String myName, String youName) {
	//		Cursor cursor = DataSupport.findBySQL("select * from user_msg where username=? and other_username=? and dir_type = ?",
	//				myName, youName,String.valueOf(DBConstants.DIR_TYPE_Y));
	//		ArrayList<Integer> count = new ArrayList<>();
	//		while (cursor.moveToNext()) {
	//			int is_read = cursor.getInt(cursor.getColumnIndex("is_read"));
	//			if (is_read == 0) {
	//				count.add(is_read);
	//			}
	//		}
		List<user_msg> read = LitePal.select("is_read")
				.where("username = ? and other_username =? and dir_type = ? and is_read =? ", myName, youName,String.valueOf(DBConstants.DIR_TYPE_Y),String.valueOf(DBConstants.READ_N)).find(user_msg.class);
		return read.size();// 未读条数
	}

	/**
	 * 根据当前用户名找所有未读消息
	 * 
	 * @return
	 */
	public int findMessageReadAll() {
		String myName = WiFiChat.instance().getMe().getUsername();
		if (null == myName || TextUtils.isEmpty(myName)) {
			return 0;
		}
		List<user_msg> readAll = LitePal.select("is_read").where("username = ? and dir_type = ? and is_read = ?", myName,String.valueOf(DBConstants.DIR_TYPE_Y),String.valueOf(DBConstants.READ_N)).find(user_msg.class);
		return readAll.size();

	}

	/**
	 * 跟新已读状态
	 * 
	 * @param youName
	 */
	public void updateReadStatus(String youName) {
		String myName = WiFiChat.instance().getMe().getUsername();
		if (null == youName || TextUtils.isEmpty(youName)) {
			return;
		}
		if (null == myName || TextUtils.isEmpty(myName)) {
			return;
		}
		user_msg user = new user_msg();
		user.setIs_read(1);// 已读
		user.updateAll("username = ? and other_username =?", myName, youName);
//		List<user_msg> msgUsers = DataSupport.select("is_read")
//				.where("username = ? and other_username =?", myName, youName).find(user_msg.class);
	}

	/**
	 * 跟新发送状态
	 * 
	 * @param myName
	 * @param youName
	 *            0正在发送,1发送错误，2对方已经收到
	 */

	public static void updateSendStatus(String myName, String youName, int msg_id, int status) {
		user_msg user = new user_msg();
		user.setStatus(status);// 已读
		user.updateAll("username = ? and other_username = ? and msg_id = ?", myName, youName, msg_id + "");
	}

	/**
	 * 跟新下载状态 ，暂用不上
	 * 
	 * @param fileUUID
	 * @param type
	 */
	public void updateDownloadStatus(String fileUUID, int type) {
		msg_attachment attach = new msg_attachment();
		attach.setStatus(type);
		attach.updateAll("file_uuid =? ", fileUUID);
	}
}
