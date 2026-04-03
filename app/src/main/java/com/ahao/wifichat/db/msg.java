package com.ahao.wifichat.db;

import java.util.ArrayList;
import java.util.List;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

public class msg extends LitePalSupport {
	private int id;//自增ID
	private int type;//消息类型
	private String content;//消息内容。
	private long msg_time;//消息发送时间或接收时间
	private List<user_msg> userList = new ArrayList<user_msg>();
	private msg_attachment msg_att;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getMsg_time() {
		return msg_time;
	}

	public void setMsg_time(long msg_time) {
		this.msg_time = msg_time;
	}

	public List<user_msg> getUserList() {
		return userList;
	}

	public void setUserList(List<user_msg> userList) {
		this.userList = userList;
	}

	public msg_attachment getMsg_att() {
		return msg_att;
	}
	public msg_attachment queryMsg_att() {
		return LitePal.where("msg_id =? ",String.valueOf(getId())).findFirst(msg_attachment.class);
	}
	public void setMsg_att(msg_attachment msg_att) {
		this.msg_att = msg_att;
	}
	
	
}
