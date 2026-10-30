package com.ahao.wifichat.protocol

/** 数据包流水号生成器，溢出后回绕到 0 */
object SerialNumberUtils {
    private var sn = 0

    @Synchronized
    fun next(): Int {
        sn = (sn + 1).takeIf { it >= 0 } ?: 0
        return sn
    }
}
