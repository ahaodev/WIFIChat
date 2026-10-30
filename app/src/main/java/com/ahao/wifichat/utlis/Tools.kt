package com.ahao.wifichat.utlis

import com.ahao.wifichat.net.Constants
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger

/** 通用工具方法 */
object Tools {

    private val LOG = Logger.getLogger(Tools::class.java.name)
    private val VALID_NICK = Regex("[\\p{Alnum}[-_]]{1,10}")

    /** 时间戳 [HH:MM:SS] */
    fun getTime(): String {
        val cal = Calendar.getInstance()
        return "[${getDoubleDigit(cal.get(Calendar.HOUR_OF_DAY))}:" +
            "${getDoubleDigit(cal.get(Calendar.MINUTE))}:${getDoubleDigit(cal.get(Calendar.SECOND))}]"
    }

    /** 小于 10 的数字前补 0 */
    fun getDoubleDigit(number: Int): String = if (number < 10) "0$number" else "$number"

    /** 按格式输出日期，[date] 为空时使用当前时间 */
    fun dateToString(date: Date?, format: String): String =
        SimpleDateFormat(format, Locale.ENGLISH).format(date ?: Date())

    fun decimalFormat(format: String, number: Double): String = DecimalFormat(format).format(number)

    fun isValidNick(nick: String?): Boolean = nick != null && VALID_NICK.matches(nick)

    /** 字节数转为 KB / MB 字符串 */
    fun byteToString(bytes: Long): String {
        var kbSize = bytes / 1024.0
        return if (kbSize > 1024) {
            kbSize /= 1024
            decimalFormat("0.00", kbSize) + "MB"
        } else {
            decimalFormat("0.00", kbSize) + "KB"
        }
    }

    /** 按 [Constants.MESSAGE_CHARSET] 编码后的字节长度 */
    fun getBytes(text: String): Int = text.toByteArray(charset(Constants.MESSAGE_CHARSET)).size

    fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            LOG.log(Level.WARNING, e.toString())
        }
    }

    fun capitalizeFirstLetter(word: String): String =
        if (word.isEmpty()) word else word.substring(0, 1).uppercase() + word.substring(1)

    fun shorten(word: String, length: Int): String = when {
        length < 0 -> ""
        word.length <= length -> word
        else -> word.substring(0, length)
    }

    /** 扩展名（含点），没有则返回空串 */
    fun getFileExtension(filename: String?): String? {
        filename ?: return null
        val dotIndex = filename.lastIndexOf(".")
        return if (dotIndex == -1) "" else filename.substring(dotIndex)
    }

    /** 去掉扩展名的文件名 */
    fun getFileBaseName(filename: String?): String? {
        filename ?: return null
        val dotIndex = filename.lastIndexOf(".")
        return if (dotIndex == -1) filename else filename.substring(0, dotIndex)
    }

    fun percent(fraction: Double, total: Double): Double = (100.0 / total) * fraction

    fun percentOf(percent: Double, total: Double): Double = (percent / 100.0) * total

    fun postPadString(stringToPad: String, paddedLength: Int): String =
        stringToPad.padEnd(paddedLength)

    fun isEmpty(string: String?): Boolean = string == null || string.trim().isEmpty()

    fun appendSlash(path: String): String {
        val fileSeparator = System.getProperty("file.separator")
        return if (path.endsWith(fileSeparator!!)) path else path + fileSeparator
    }

    /** 若文件已存在，则生成 `name_1.ext`、`name_2.ext`… 形式的新文件 */
    fun getFileWithIncrementedName(existingFile: File): File {
        if (!existingFile.exists()) return existingFile

        val path = existingFile.parent?.let { it + File.separator } ?: ""
        var counter = 1
        var newFile: File
        do {
            val existingName = existingFile.name
            newFile = File("$path${getFileBaseName(existingName)}_$counter${getFileExtension(existingName)}")
            counter++
        } while (newFile.exists())
        return newFile
    }

    fun emptyIfNull(string: String?): String = string ?: ""
}
