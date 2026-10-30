package com.ahao.wifichat.utlis

import android.annotation.SuppressLint
import android.media.MediaPlayer

/** 语音播放器，播放结束时通过 [OnPlayListener] 回调文件路径 */
@SuppressLint("SimpleDateFormat")
class AudioPlayer(private val playListener: OnPlayListener? = null) : MediaPlayer.OnCompletionListener {

    fun interface OnPlayListener {
        fun onStop(path: String?)
    }

    private var player: MediaPlayer? = null

    var recordPath: String? = null
        private set

    var isRun: Boolean = false
        private set

    fun startPlay(path: String) {
        isRun = true
        release()
        recordPath = path
        val mp = MediaPlayer().also { player = it }
        runCatching {
            mp.reset()
            mp.setDataSource(path)
            mp.prepare()
            mp.start()
            mp.setOnCompletionListener(this)
        }.onFailure { release() }
    }

    /** 回退 1 秒 */
    fun changePlay() {
        val mp = player?.takeIf { isRun } ?: return
        runCatching { mp.seekTo(mp.currentPosition - 1000) }.onFailure { it.printStackTrace() }
    }

    fun stopPlay() {
        if (player == null || !isRun) return
        release()
        isRun = false
        recordPath = null
    }

    override fun onCompletion(mp: MediaPlayer) {
        isRun = false
        val path = recordPath
        recordPath = null
        playListener?.onStop(path)
    }

    private fun release() {
        player?.let { mp -> runCatching { mp.release() }.onSuccess { player = null } }
    }
}
