package com.ahao.wifichat.utlis

import com.ahao.wifichat.protocol.v1.MessageInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

/** JSON 解析工具（基于 Gson，类型信息通过 reified 泛型获取） */
object GsonUtils {

    @PublishedApi
    internal val gson = Gson()

    /** 解析为 `MessageInfo<T>` */
    inline fun <reified T> fromJsonObject(json: String): MessageInfo<T>? =
        gson.fromJson(json, gsonTypeOf<MessageInfo<T>>())

    /** 解析为 `MessageInfo<List<T>>` */
    inline fun <reified T> fromJsonArray(json: String): MessageInfo<List<T>>? =
        gson.fromJson(json, gsonTypeOf<MessageInfo<List<T>>>())

    /** 直接解析为实体 */
    inline fun <reified T> getJson(json: String): T? = gson.fromJson(json.trim(), gsonTypeOf<T>())

    /** 取出 JSON 对象中名为 [key] 的字段并解析为实体 */
    inline fun <reified T> getJson(json: String, key: String): T? =
        try {
            gson.fromJson<T>(JSONObject(json).getString(key).trim(), gsonTypeOf<T>())
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }

    /** 取出 JSON 对象中名为 [listKey] 的数组并解析为列表 */
    fun <T> getJsonArrays(json: String, listKey: String, type: Type): List<T>? {
        val arr = JSONObject(json).getJSONArray(listKey).toString()
        return gson.fromJson(arr, type)
    }

    @PublishedApi
    internal inline fun <reified T> gsonTypeOf(): Type = object : TypeToken<T>() {}.type
}

/** 序列化为 JSON 字符串（复用同一个 Gson 实例） */
fun Any.toJson(): String = GsonUtils.gson.toJson(this)
