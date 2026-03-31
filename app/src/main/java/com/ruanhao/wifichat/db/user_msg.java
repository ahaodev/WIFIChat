package com.ruanhao.wifichat.db;

import org.litepal.crud.LitePalSupport;

public class user_msg extends LitePalSupport {
	private int id;//自增id
	private String username;//用户名
//	private int msg_id;//消息id
	private int dir_type;//消息方向。
	private String other_username;//对方用户名。
	private String other_name;//对方姓名 
	private int status;//消息发送状态，该字段只对当前用户发送的消息有效。
	private int is_read;//当前用户是否已读消息，该字段只对当前用户收到的消息有效
	private msg message;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getMsg_id() {
		return 0;//msg_id;
	}

	public void setMsg_id(int msg_id) {
//		this.msg_id = msg_id;
	}

	public int getDir_type() {
		return dir_type;
	}

	public void setDir_type(int dir_type) {
		this.dir_type = dir_type;
	}

	public String getOther_username() {
		return other_username;
	}

	public void setOther_username(String other_username) {
		this.other_username = other_username;
	}

	public String getOther_name() {
		return other_name;
	}

	public void setOther_name(String other_name) {
		this.other_name = other_name;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getIs_read() {
		return is_read;
	}

	public void setIs_read(int is_read) {
		this.is_read = is_read;
	}

	public msg getMessageTab() {
		return message;
	}

	public void setMessageTab(msg message) {
		this.message = message;
	}
}
