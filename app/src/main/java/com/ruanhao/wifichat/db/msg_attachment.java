package com.ruanhao.wifichat.db;

import org.litepal.crud.LitePalSupport;

public class msg_attachment extends LitePalSupport {
	private int id;// 系统自增ID
//	private int msg_id;// 消息id（这里为附件通知消息的id）
	private String file_uuid;//附件uuid
	private int type;// 附件类型。
	private String filename;// 文件名称
	private long size;// 文件大小，单位：byte
	private String uri;// 文件本地存储路径。 
	private int status;// 文件下载状态，对于当前用户接收文件有效
//	private msg message;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMsg_id() {
		return 0;//msg_id;
	}

	public void setMsg_id(int msg_id) {
//		this.msg_id = msg_id;
	}

	public String getFile_uuid() {
		return file_uuid;
	}

	public void setFile_uuid(String file_uuid) {
		this.file_uuid = file_uuid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

//	public msg getMessage() {
//		return message;
//	}
//
//	public void setMessage(msg message) {
//		this.message = message;
//	}
	
	
}
