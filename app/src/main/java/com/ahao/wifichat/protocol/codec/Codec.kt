package com.ahao.wifichat.protocol.codec

/** 数据包编解码：`版本:流水号:发送者:保留:命令字:内容` */
object Codec {
    const val SPLIT_MAX = 6
    const val SPLIT = ":"

    fun encode(pack: Pack): String = with(pack) {
        listOf(version, sn, sendName, temp, command, content).joinToString(SPLIT)
    }

    /** 解码；内容中允许出现分隔符，因此按最大段数切分 */
    fun decode(data: String): Pack {
        val parts = data.split(SPLIT, limit = SPLIT_MAX)
        return Pack(
            version = parts[0].toInt(),
            sn = parts[1].toInt(),
            sendName = parts[2],
            temp = parts[3],
            command = parts[4].toInt(),
            content = parts[5],
        )
    }
}
