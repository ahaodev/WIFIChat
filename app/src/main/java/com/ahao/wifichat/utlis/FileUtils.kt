package com.ahao.wifichat.utlis

import java.io.File

object FileUtils {
    /** 判断文件是否存在；路径非法或无权限时返回 false */
    fun isFileExists(path: String): Boolean = runCatching { File(path).exists() }.getOrDefault(false)
}
