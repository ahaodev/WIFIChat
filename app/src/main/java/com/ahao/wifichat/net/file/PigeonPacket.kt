package com.ahao.wifichat.net.file

/** 旧版协议数据包：`版本:包序号:用户名:主机名:命令字:消息` */
class PigeonPacket(
    val version: String,
    val packetNumber: String,
    val username: String,
    val hostname: String,
    val command: Int,
    val message: String?,
) {
    companion object {
        fun parse(bin: String): PigeonPacket {
            val segments = bin.split(":", limit = 6)
            return PigeonPacket(
                version = segments[0],
                packetNumber = segments[1],
                username = segments[2],
                hostname = segments[3],
                command = segments[4].toInt(),
                message = segments.getOrNull(5),
            )
        }
    }
}
