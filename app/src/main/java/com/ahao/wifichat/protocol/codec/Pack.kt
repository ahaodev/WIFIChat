package com.ahao.wifichat.protocol.codec;


import java.io.Serializable;

public class Pack implements Serializable {
	
	private static final long serialVersionUID = 8203179441328456641L;
	// 版本号
	private int version;
	// 消息流水号
	private int sn;
	// 发送者用户名
	private String sendname = "";
	// 保留字段
	private String temp = "";
	// 命令字
	private int command;
	//消息内容
	private String content = "";

	public Pack() {

	}

	public Pack(int version, int sn, String sendname, int command, String content) {
		
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setSn(int sn) {
		this.sn = sn;
	}
	
	public int getSn() {
		return sn;
	}
	
	public void setSendName(String name) {
		this.sendname = name;
	}
	
	public String getSendName() {
		return sendname;
	}
	
	public void setTemp(String temp) {
		this.temp = temp;
	}
	
	public String getTemp() {
		return temp;
	}
	
	public void setCommand(int command) {
		this.command = command;
	}
	
	public int getCommand() {
		return command;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
}
