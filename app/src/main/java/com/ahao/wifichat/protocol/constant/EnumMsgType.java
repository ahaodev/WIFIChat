package com.ahao.wifichat.protocol.constant;

/**
 * Created by xiang.shen on 2017年5月8日.
 *
 */
public enum EnumMsgType {
	TXT(1, "文本"), 
	POS(2, "位置"), 
	ATTACHMENT(3, "附件");

	private int value;
	private String tip;

	EnumMsgType(int value, String tip) {
		this.value = value;
		this.tip = tip;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

	public static EnumMsgType fromValue(int value) {
		EnumMsgType retEm = null;
		EnumMsgType[] values = EnumMsgType.values();
		for (EnumMsgType em : values) {
			if (em.value == value) {
				retEm = em;
				break;
			}
		}
		return retEm;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(tip).append("[").append(value).append("]").toString();
	}
}
