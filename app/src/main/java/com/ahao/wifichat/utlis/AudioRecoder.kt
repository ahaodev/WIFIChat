package com.ahao.wifichat.utlis

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/** 语音录制器（AMR，最长 60 秒） */
@SuppressLint("SimpleDateFormat")
class AudioRecoder {

    var startTime: Long = 0L
        private set

    /** 录音时长（秒） */
    var duration: Long = 0L
        private set

    var recordPath: String? = null
        private set

    var isRun: Boolean = false
        private set

    private var recorder: MediaRecorder? = null

    fun startRecord(ctx: Context) {
        isRun = true

        val date = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
        val file = File(ctx.getExternalFilesDir("chat"), "RecordFile_$date.amr")
        recordPath = file.absolutePath
        if (file.exists()) file.delete()

        release()

        val rec = MediaRecorder()
        recorder = rec
        val started = runCatching {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            rec.setMaxDuration(60 * 1000)
            rec.setOutputFile(recordPath)
            rec.prepare()
            rec.start()
        }.onFailure { it.printStackTrace() }.isSuccess
        if (!started) {
            release()
            isRun = false
            return
        }
        duration = 0L
        startTime = System.currentTimeMillis()
    }

    fun stopRecord() {
        val rec = recorder ?: return
        if (!isRun) return

        duration = (System.currentTimeMillis() - startTime) / 1000

        runCatching {
            rec.stop()
            release()
        }.onFailure { it.printStackTrace() }
        isRun = false
    }

    val amplitude: Int get() = recorder?.maxAmplitude ?: 0

    private fun release() {
        recorder?.let { rec ->
            runCatching {
                rec.release()
                recorder = null
            }.onFailure { it.printStackTrace() }
        }
    }
}
