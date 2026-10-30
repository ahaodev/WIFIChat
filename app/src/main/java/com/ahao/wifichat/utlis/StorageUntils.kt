package com.ahao.wifichat.utlis

import android.os.Environment
import java.io.File

object StorageUntils {

    fun hasStorage(requireWriteAccess: Boolean): Boolean =
        when (Environment.getExternalStorageState()) {
            Environment.MEDIA_MOUNTED -> !requireWriteAccess || checkFsWritable()
            Environment.MEDIA_MOUNTED_READ_ONLY -> !requireWriteAccess
            else -> false
        }

    /** 通过在 DCIM 目录下检查可写性，确认卷是否真正可写 */
    private fun checkFsWritable(): Boolean {
        val directory = File("${Environment.getExternalStorageDirectory()}/DCIM")
        if (!directory.isDirectory && !directory.mkdirs()) return false
        return directory.canWrite()
    }
}
