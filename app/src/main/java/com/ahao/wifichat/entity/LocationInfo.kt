package com.ahao.wifichat.entity

import com.google.gson.Gson

/** 位置信息，序列化为 JSON */
data class LocationInfo(
    var lon: String? = null,
    var lat: String? = null,
) {
    override fun toString(): String = Gson().toJson(this)
}
