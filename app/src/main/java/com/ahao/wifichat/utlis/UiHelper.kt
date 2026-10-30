package com.ahao.wifichat.utlis;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class UiHelper {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 取一个状态
	 * 
	 * @param context
	 * @param name
	 * @param state
	 * @return
	 */
	public static boolean getShareBooleanState(Context context, String name, Boolean state) {
		SharedPreferences sp = context.getSharedPreferences("STATE", Context.MODE_PRIVATE);
		return sp.getBoolean(name, state);
	}

	/**
	 * 存一个状态
	 * 
	 * @param context
	 * @param name
	 * @param state
	 */
	public static void setShareBooleanState(Context context, String name, boolean state) {
		SharedPreferences sp = context.getSharedPreferences("STATE", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean(name, state);
		editor.commit();
	}

	/**
	 * 往Shared中存数据的方法
	 * 
	 * @param context
	 *            上下文对象
	 * @param filename
	 *            文件名字
	 * @param key
	 *            存放数据的健
	 * @param value
	 *            存放数据的值
	 */
	public static void setShareData(Context context, String filename, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param key
	 *            键
	 * @param filename
	 *            文件名
	 * @return
	 */
	public static String getShareData(Context context, String key, String filename) {
		SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
		return sp.getString(key, "");
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param key
	 *            键
	 * @param filename
	 *            文件名
	 * @param defInteger
	 *            缺省值
	 * @return
	 */
	public static int getShareIntgerData(Context context, String key, String filename, int defInteger) {
		SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
		return sp.getInt(key, defInteger);
	}
}
