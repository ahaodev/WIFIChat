package com.ahao.wifichat.utlis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ahao.wifichat.protocol.ParameterizedTypeImpl;
import com.ahao.wifichat.protocol.v1.MessageInfo;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GsonUtils {

	/**
	 * 
	 * @param json json字符串
	 * @param clazz 类
	 * @return
	 */
	public static <Fields> MessageInfo<Fields> fromJsonObject(String json, Class<Fields> clazz) {
		Gson gson = new Gson();
	    Type type = new ParameterizedTypeImpl(MessageInfo.class, new Class[]{clazz});
	    return gson.fromJson(json, type);
	}
	
	/**
	 * 
	 * @param json json字符串
	 * @param clazz 类
	 * @return
	 */
	public static <Fields> MessageInfo<List<Fields>> fromJsonArray(String json, Class<Fields> clazz) {
		Gson gson = new Gson();
		// 生成List<T> 中的 List<T>
		Type listType = new ParameterizedTypeImpl(List.class, new Class[] { clazz });
		// 根据List<T>生成完整的Result<List<T>>
		Type type = new ParameterizedTypeImpl(MessageInfo.class, new Type[] { listType });
		return gson.fromJson(json, type);
	}
	
	
	/**
	 * 
	 * @param json
	 *            整体对象
	 * @param listjson
	 *            要提取的Array
	 * @param type
	 *            将转换成的类型
	 * @return 返回arraylist实体
	 * @throws JSONException
	 *             异常交给error回调
	 */
	public static <T> ArrayList<T> getJsonarrays(String json, String listjson, Type type) throws JSONException {
		Gson gson = new Gson();
		JSONObject obj = new JSONObject(json);
		String arr = obj.getJSONArray(listjson).toString();
		ArrayList<T> list = gson.fromJson(arr, type);
		return list;
	}

	/**
	 * 
	 * @param json
	 *            String 串
	 * @param type
	 *            类型
	 * @return
	 * @throws JSONException
	 */
	public static <T> ArrayList<T> getJsonarrays(String json, Type type) throws JSONException {
		Gson gson = new Gson();
		ArrayList<T> list = gson.fromJson(json, type);
		return list;
	}

	/**
	 * 直接转换成单个实体类
	 * 
	 * @param json
	 * @param t
	 * @return
	 * @throws JSONException
	 */
	public static <T> T getJson(String json, Class<T> t)   {
		Gson gson = new Gson();
		T bean = gson.fromJson(json.trim(), t);
		return bean;
	}
	public static <T> T getJson(String json,String str,Class<T>t) {
		JSONObject obj = null;
		Gson gson = new Gson();
		T bean = null;
		try {
			obj = new JSONObject(json);
			bean = gson.fromJson(obj.getString(str).trim(), t);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bean;
	}
	public <T> T getjsonNoNull(String json, Class<T> t) {
		Gson gson = new GsonBuilder().registerTypeAdapter(t, STRING).serializeNulls().create();
		return gson.fromJson(json, t);
	}

	/**
	 * 自定义TypeAdapter ,null对象将被解析成空字符串
	 */
	public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
		public String read(JsonReader reader) {
			try {
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return "";// 原先是返回Null，这里改为返回空字符串
				}
				return reader.nextString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

		public void write(JsonWriter writer, String value) {
			try {
				if (value == null) {
					writer.nullValue();
					return;
				}
				writer.value(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
