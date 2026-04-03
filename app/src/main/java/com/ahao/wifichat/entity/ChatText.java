package com.ahao.wifichat.entity;

import com.google.gson.Gson;

/**
 * 文本消息正文
 * 
 * @author hao.ruan
 *
 */
public class ChatText {

	String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
