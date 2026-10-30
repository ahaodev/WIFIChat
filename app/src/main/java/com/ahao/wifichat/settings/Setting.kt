package com.ahao.wifichat.settings

/**
 * 可变更的设置项标识（替代 Java 中的“伪枚举”）。
 * 通过 [name] 判断相等性。
 */
class Setting private constructor(private val name: String) {

    override fun equals(other: Any?): Boolean = this === other || (other is Setting && name == other.name)

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = name

    companion object {
        /** 对应 [Settings.isLogging] */
        val LOGGING = Setting("LOGGING")
    }
}
