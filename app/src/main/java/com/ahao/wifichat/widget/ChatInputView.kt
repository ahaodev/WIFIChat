package com.ahao.wifichat.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.adapter.CommonAdapter
import com.ahao.wifichat.ui.adapter.ViewHolder
import com.ahao.wifichat.utlis.AudioRecoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** 聊天输入区：文本输入、按住说话录音、附加功能面板 */
class ChatInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    interface InputClickListener {
        fun sendImage()
        fun sendVideo()
        fun sendLocation()
        fun sendFile()
        fun sendMessage(message: String)
    }

    interface AudioRecordCallback {
        fun onRecording(iTime: Int, amplitude: Int)
        fun onStartRecord()
        fun onReadyCancel()
        fun onReturnNormal()
        fun onStopRecord()
        fun recodEnd(path: String?)
    }

    private class AdditionItem(val textId: Int, val resId: Int)

    var inputClickListener: InputClickListener? = null
    var audioCallback: AudioRecordCallback? = null

    /** 附加面板展开时回调（用于滚动消息列表） */
    var onAdditionExpanded: (() -> Unit)? = null

    /** 没有录音权限时回调，由宿主 Activity 申请 */
    var onRequestRecordPermission: (() -> Unit)? = null

    private val audioRecorder = AudioRecoder()
    private val scope = MainScope()
    private var recordJob: Job? = null
    private var screenHeight = 0
    private var needStartRecorder = false
    private var readyToCancel = false
    private var downY = 0f

    private val vibrator = context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val btnAddition: ImageButton
    private val btnSend: Button
    private val btnSpeak: Button
    private val btnSwitch: ImageButton
    private val input: TextView
    private val grid: GridView

    private val additionItems = listOf(
        AdditionItem(R.string.photo, R.drawable.icon_chat_add_photo),
        AdditionItem(R.string.video, R.drawable.icon_chat_add_video),
        AdditionItem(R.string.location, R.drawable.icon_chat_add_location),
        AdditionItem(R.string.File, R.drawable.icon_chat_add_file),
    )

    init {
        View.inflate(context, R.layout.p2p_view_chat_input, this)

        btnAddition = findViewById(R.id.btnAddition)
        btnSend = findViewById(R.id.btnSend)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnSwitch = findViewById(R.id.btnSwitch)
        input = findViewById(R.id.TxtInput)
        grid = findViewById(R.id.p2p_chat_input_grid)

        btnAddition.setOnClickListener { onAdditionClicked() }
        btnSend.setOnClickListener {
            val text = input.text.toString()
            input.text = ""
            inputClickListener?.sendMessage(text)
        }
        btnSwitch.setOnClickListener { onSwitchClicked() }
        input.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) hideAdditionPanel()
            false
        }
        btnSpeak.setOnTouchListener { _, event -> onSpeakTouch(event) }
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val hasText = input.text.isNotEmpty()
                btnAddition.visibility = if (hasText) View.GONE else View.VISIBLE
                btnSend.visibility = if (hasText) View.VISIBLE else View.GONE
            }
        })

        grid.adapter = object : CommonAdapter<AdditionItem>(context, additionItems, R.layout.p2p_view_chat_input_textview) {
            override fun convert(helper: ViewHolder, item: AdditionItem?) {
                item ?: return
                (helper.convertView as? TextView)?.apply {
                    setCompoundDrawablesWithIntrinsicBounds(null, context.getDrawable(item.resId), null, null)
                    setText(item.textId)
                    setOnClickListener {
                        when (item.resId) {
                            R.drawable.icon_chat_add_photo -> inputClickListener?.sendImage()
                            R.drawable.icon_chat_add_video -> inputClickListener?.sendVideo()
                            R.drawable.icon_chat_add_location -> inputClickListener?.sendLocation()
                            R.drawable.icon_chat_add_file -> inputClickListener?.sendFile()
                        }
                    }
                }
            }
        }
        hideKeyboard()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        screenHeight = context.resources.displayMetrics.heightPixels
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDetachedFromWindow() {
        scope.cancel()
        super.onDetachedFromWindow()
    }

    fun hideAdditionPanel() {
        grid.visibility = View.GONE
    }

    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(applicationWindowToken, 0)
    }

    private fun onAdditionClicked() {
        hideKeyboard()
        if (grid.visibility == View.GONE) {
            grid.visibility = View.VISIBLE
            onAdditionExpanded?.invoke()
        } else {
            grid.visibility = View.GONE
        }
    }

    private fun onSwitchClicked() {
        hideAdditionPanel()
        if (btnSpeak.visibility != View.GONE) {
            btnSwitch.setImageResource(R.drawable.chat_input_voice)
            btnSpeak.visibility = View.GONE
        } else {
            btnSwitch.setImageResource(R.drawable.chat_input_keyboard)
            btnSpeak.visibility = View.VISIBLE
            input.text = ""
            hideKeyboard()
        }
    }

    /** 按住说话：按下开始录音，上滑取消，松开发送 */
    private fun onSpeakTouch(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    onRequestRecordPermission?.invoke()
                    return false
                }
                downY = event.y
                needStartRecorder = true
                readyToCancel = false
                btnSpeak.setText(R.string.release_end)
                btnSpeak.setBackgroundResource(R.drawable.details_sendmsg_btn)
                startRecordingLoop()
            }
            MotionEvent.ACTION_UP -> {
                if (readyToCancel) {
                    needStartRecorder = false
                    btnSpeak.setText(R.string.down_speak)
                    recordJob?.cancel()
                    audioRecorder.stopRecord()
                    btnSpeak.setBackgroundResource(R.drawable.input_btn_normal)
                    audioCallback?.onStopRecord()
                } else {
                    needStartRecorder = false
                    sendVoice()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val moveDistance = screenHeight / 10
                if (downY - event.y >= moveDistance) {
                    if (!readyToCancel) {
                        vibrate()
                        btnSpeak.setText(R.string.release_cancel)
                        audioCallback?.onReadyCancel()
                    }
                    readyToCancel = true
                } else {
                    if (readyToCancel) {
                        audioCallback?.onReturnNormal()
                        btnSpeak.setText(R.string.release_end)
                    }
                    readyToCancel = false
                }
            }
        }
        return false
    }

    /** 延迟 150ms 后开始录音，期间每 100ms 回调一次进度，满 60 秒自动发送 */
    private fun startRecordingLoop() {
        recordJob?.cancel()
        recordJob = scope.launch {
            delay(150)
            while (needStartRecorder) {
                if (!audioRecorder.isRun) {
                    vibrate()
                    audioCallback?.onStartRecord()
                    audioRecorder.startRecord(context.applicationContext)
                }
                val elapsedSeconds = ((System.currentTimeMillis() - audioRecorder.startTime) / 1000).toInt()
                audioCallback?.onRecording(elapsedSeconds, audioRecorder.amplitude)
                if (elapsedSeconds >= 60) {
                    sendVoice()
                    btnSpeak.isSelected = false
                    break
                }
                delay(100)
            }
        }
    }

    private fun sendVoice() {
        vibrate()
        btnSpeak.setText(R.string.down_speak)
        btnSpeak.setBackgroundResource(R.drawable.input_btn_normal)
        recordJob?.cancel()
        if (!audioRecorder.isRun) return

        audioRecorder.stopRecord()
        audioCallback?.onStopRecord()
        if (audioRecorder.duration > 0) {
            audioCallback?.recodEnd(audioRecorder.recordPath)
        } else {
            Toast.makeText(context, "录制时间太短", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }
}
