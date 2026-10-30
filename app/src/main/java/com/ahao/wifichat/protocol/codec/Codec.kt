package com.ahao.wifichat.protocol.codec;


/** 信息编码类 */
public class Codec {

	public static final int SPLIT_MAX = 6;
	public static final String SPLIT = ":";

	/** 编码 */
	public static StringBuilder doEncode(Pack Pack) {
		StringBuilder out = new StringBuilder()
				.append(Pack.getVersion()).append(SPLIT)
				.append(Pack.getSn()).append(SPLIT)
				.append(Pack.getSendName()).append(SPLIT)
				.append(Pack.getTemp()).append(SPLIT)
				.append(Pack.getCommand()).append(SPLIT)
				.append(Pack.getContent());
		return out;
	}
	
	/** 解码 */
	public static Pack doDecode(String data) {
		Pack pack = null;
		String[] datas = data.split(SPLIT, SPLIT_MAX);
		if (datas.length > 0 && datas.length <= SPLIT_MAX) {
			pack = new Pack();
			pack.setVersion(Integer.parseInt(datas[0]));
			pack.setSn(Integer.valueOf(datas[1]));
			pack.setSendName(datas[2]);
			pack.setTemp(datas[3]);
			pack.setCommand(Integer.valueOf(datas[4]));
			pack.setContent(datas[5]);
		}
		return pack;
	}
}
