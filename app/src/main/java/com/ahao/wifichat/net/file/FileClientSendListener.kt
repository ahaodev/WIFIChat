package com.ahao.wifichat.net.file

interface FileClientSendListener {
    fun onStart(state: FileState)
    fun onProgress(progress: Int)
    fun onSuccess(state: FileState)
    fun onError(e: Exception)
}
