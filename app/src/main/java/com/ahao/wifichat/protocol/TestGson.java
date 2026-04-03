package com.ahao.wifichat.protocol;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.ahao.wifichat.protocol.v1.MessageInfo;

public class TestGson {
	
	public static <T> MessageInfo<T> fromJsonObject(String reader, Class<T> clazz) {
		Gson gson = new Gson();
	    Type type = new ParameterizedTypeImpl(MessageInfo.class, new Class[]{clazz});
	    return gson.fromJson(reader, type);//GSON.fromJson(reader, type);
	}
	
	public static <T> MessageInfo<List<T>> fromJsonArray(String reader, Class<T> clazz) {
		Gson gson = new Gson();
		// 生成List<T> 中的 List<T>
		Type listType = new ParameterizedTypeImpl(List.class, new Class[] { clazz });
		// 根据List<T>生成完整的Result<List<T>>
		Type type = new ParameterizedTypeImpl(MessageInfo.class, new Type[] { listType });
		return gson.fromJson(reader, type);
	}
}
