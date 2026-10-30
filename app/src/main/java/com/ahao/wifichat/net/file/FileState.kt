package com.ahao.wifichat.net.file

/** 文件传输状态（发送或接收） */
data class FileState(
    var fileSize: Long = 0L,
    var currentSize: Long = 0L,
    var fileName: String? = null,
    var percent: Int = 0,
    var type: Int = 0,
)
