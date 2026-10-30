package com.ahao.wifichat.utlis

import java.util.Date

object DateTools {

    fun currentDateToString(format: String): String = dateToString(null, format)

    fun dateToString(date: Date?, format: String): String = Tools.dateToString(date, format)

    /** 返回从 [then] 到现在经过的时间，格式为 `N days, HH:mm:ss` */
    fun howLongFromNow(then: Long): String {
        if (then == 0L) return "0 days, 00:00:00"

        val totSec = (System.currentTimeMillis() - then) / 1000
        val days = totSec / ONE_DAY
        val hours = (totSec - days * ONE_DAY) / ONE_HOUR
        val minutes = (totSec - days * ONE_DAY - hours * ONE_HOUR) / ONE_MINUTE
        val seconds = totSec - days * ONE_DAY - hours * ONE_HOUR - minutes * ONE_MINUTE
        return "$days days, ${Tools.getDoubleDigit(hours.toInt())}:" +
            "${Tools.getDoubleDigit(minutes.toInt())}:${Tools.getDoubleDigit(seconds.toInt())}"
    }

    private const val ONE_DAY = 86400L
    private const val ONE_HOUR = 3600L
    private const val ONE_MINUTE = 60L
}
