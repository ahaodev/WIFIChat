package com.ahao.wifichat.ui.chat

import android.view.View
import android.widget.TextView
import com.ahao.wifichat.R
import com.ahao.wifichat.widget.ChatInputView
import kotlin.math.log10

/** 录音过程中的界面反馈（音量、倒计时、取消提示） */
class ChatAudioListener(private val chat: ChatActivity) : ChatInputView.AudioRecordCallback {

    private val volumeLevels = intArrayOf(
        R.drawable.voice_record_1, R.drawable.voice_record_2, R.drawable.voice_record_3,
        R.drawable.voice_record_4, R.drawable.voice_record_5, R.drawable.voice_record_6,
        R.drawable.voice_record_7, R.drawable.voice_record_8,
    )

    override fun onRecording(iTime: Int, amplitude: Int) {
        // 振幅转分贝，再分为 8 个音量级别
        val ratio = amplitude / 600
        var db = if (ratio > 1) (20 * log10(ratio.toDouble())).toInt() else 0
        db = (db / 4).coerceAtMost(7)
        chat.findViewById<TextView>(R.id.voice_volume).setBackgroundResource(volumeLevels[db])
        if (iTime >= 50) {
            chat.findViewById<TextView>(R.id.tv_voice_time_count_down).text = "时间过短"
        }
    }

    override fun onStartRecord() {
        chat.findViewById<TextView>(R.id.voice_volume).setBackgroundResource(R.drawable.voice_record_3)
        chat.findViewById<View>(R.id.recordimage).visibility = View.VISIBLE
        chat.findViewById<TextView>(R.id.tv_voice_time_count_down).setText(R.string.cancel_sendvoice)
        onReturnNormal()
    }

    override fun onStopRecord() {
        chat.findViewById<View>(R.id.recordimage).visibility = View.INVISIBLE
        chat.findViewById<View>(R.id.recordimage_cancel).visibility = View.INVISIBLE
    }

    override fun onReadyCancel() {
        chat.findViewById<View>(R.id.recordimage_cancel).visibility = View.VISIBLE
        chat.findViewById<View>(R.id.recordimage).visibility = View.INVISIBLE
    }

    override fun onReturnNormal() {
        chat.findViewById<View>(R.id.recordimage).visibility = View.VISIBLE
        chat.findViewById<View>(R.id.recordimage_cancel).visibility = View.INVISIBLE
    }

    override fun recodEnd(path: String?) {
        chat.sendAudio(path)
    }
}
