package com.ahao.wifichat.utlis

import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.zip.GZIPInputStream
import kotlin.math.roundToLong

object StringUtils {

    private val dateFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd HH:mm") }
    private val dateFormatterDay = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd") }

    private const val EARTH_RADIUS = 6378137.0

    fun toDate(sdate: String): Date? = runCatching { dateFormatter.get()!!.parse(sdate) }.getOrNull()

    fun isToday(sdate: String): Boolean {
        val time = toDate(sdate) ?: return false
        val day = dateFormatterDay.get()!!
        return day.format(Date()) == day.format(time)
    }

    fun getToday(): Long = dateFormatterDay.get()!!.format(Calendar.getInstance().time).replace("-", "").toLong()

    fun getCurrentTime(): String = dateFormatter.get()!!.format(Date())

    fun formatTime(time: Long): String = dateFormatter.get()!!.format(Date(time))

    /** 空串、或仅包含空格/制表/回车/换行时返回 true */
    fun isEmpty(input: String?): Boolean =
        input == null || input.all { it == ' ' || it == '\t' || it == '\r' || it == '\n' }

    fun toInt(str: String, defValue: Int): Int = str.toIntOrNull() ?: defValue

    fun toInt(obj: Any?): Int = obj?.let { toInt(it.toString(), 0) } ?: 0

    fun toLong(obj: String): Long = obj.toLongOrNull() ?: 0L

    fun toDouble(obj: String): Double = obj.toDoubleOrNull() ?: 0.0

    fun toBool(b: String): Boolean = b.toBoolean()

    /** 读取流中的所有行并首尾相接（不保留换行） */
    fun toConvertString(input: InputStream): String =
        runCatching { input.bufferedReader().useLines { lines -> lines.joinToString("") } }
            .onFailure { it.printStackTrace() }
            .getOrDefault("")

    /** GZIP 解压为 UTF-8 字符串 */
    fun uncompress(input: InputStream?): String {
        if (input == null) return ""
        return GZIPInputStream(input).use { it.readBytes().toString(Charsets.UTF_8) }
    }

    /** MD5，返回大写十六进制字符串 */
    fun md5(s: String): String? = runCatching {
        MessageDigest.getInstance("MD5").digest(s.toByteArray()).joinToString("") { "%02X".format(it.toInt() and 0xff) }
    }.onFailure { it.printStackTrace() }.getOrNull()

    /** 两个经纬度之间的距离（米），保留整数位（与原实现一致） */
    fun gps2m(latA: Double, lngA: Double, latB: Double, lngB: Double): Double {
        val radLat1 = latA * Math.PI / 180.0
        val radLat2 = latB * Math.PI / 180.0
        val a = radLat1 - radLat2
        val b = (lngA - lngB) * Math.PI / 180.0
        val s = 2 * Math.asin(
            Math.sqrt(Math.pow(Math.sin(a / 2), 2.0) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2.0))
        ) * EARTH_RADIUS
        return ((s * 10000).roundToLong() / 10000).toDouble()
    }
}
