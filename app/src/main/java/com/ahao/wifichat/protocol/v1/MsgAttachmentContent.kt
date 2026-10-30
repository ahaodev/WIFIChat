package com.ahao.wifichat.protocol.v1;

import com.google.gson.annotations.SerializedName;

public class MsgAttachmentContent {
	int msg_id;
	@SerializedName("content")
	MsgAttachment attachment;

	public int getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(int msg_id) {
		this.msg_id = msg_id;
	}

	public MsgAttachment getAttachment() {
		return attachment;
	}

	public void setAttachment(MsgAttachment attachment) {
		this.attachment = attachment;
	}

}
