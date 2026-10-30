package com.ahao.wifichat.ui.chat.video

import android.Manifest
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.utlis.Permissions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * 视频录制（CameraX）。
 * 取景使用 [PreviewView]；录制输出 MP4 到 [VIDEO_FILEPATH]；达到 [VIDEO_TIME_LIMIT] 秒自动停止。
 * 调用方需先确保已授予相机与录音权限。
 */
class VideoRecorderActivity : BaseActivity() {

    companion object {
        const val VIDEO_FILEPATH = "filepath"
        const val VIDEO_TIME_LIMIT = "timelimit"

        /** 0 表示高画质，其它值为标清 */
        const val VIDEO_QUALITY = "quality"
    }

    private lateinit var previewView: PreviewView
    private lateinit var goBackButton: Button
    private lateinit var rePlayButton: Button
    private lateinit var recorderButton: Button
    private lateinit var switchCameraButton: Button
    private lateinit var senderButton: Button
    private lateinit var timeView: TextView
    private lateinit var imgView: ImageView

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var useFrontCamera = false
    private var timerJob: Job? = null
    private var timeCount = 0
    private var succeed = false

    private var filePath = ""
    private var timeLimit = 30
    private var quality: Quality = Quality.HD

    private val isRecording: Boolean get() = activeRecording != null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_video_recorder)

        previewView = findViewById(R.id.surface_camera)
        recorderButton = findViewById(R.id.recoderbutton)
        goBackButton = findViewById(R.id.go_back_button)
        rePlayButton = findViewById(R.id.play_button)
        switchCameraButton = findViewById(R.id.switch_camera_button)
        senderButton = findViewById(R.id.send_button)
        imgView = findViewById(R.id.img_view)
        timeView = findViewById(R.id.recorder_time)

        recorderButton.setOnClickListener { if (isRecording) stopRecording() else startRecording() }
        goBackButton.setOnClickListener { cancelAndFinish() }
        rePlayButton.setOnClickListener {
            startActivity(Intent(this, WeixinVideoPlayerActivity::class.java).putExtra("url", filePath))
        }
        switchCameraButton.setOnClickListener { switchCamera() }
        senderButton.setOnClickListener {
            if (succeed) {
                setResult(RESULT_OK, Intent(filePath))
                finish()
            }
        }
        previewView.setOnTouchListener { _, event -> onPreviewTouch(event) }

        rePlayButton.visibility = View.GONE
        switchCameraButton.visibility = View.GONE
        senderButton.visibility = View.GONE
        timeView.visibility = View.GONE
        goBackButton.visibility = View.VISIBLE
        imgView.visibility = View.GONE

        filePath = intent.getStringExtra(VIDEO_FILEPATH).orEmpty()
        timeLimit = intent.getIntExtra(VIDEO_TIME_LIMIT, 30)
        quality = if (intent.getIntExtra(VIDEO_QUALITY, 0) == 0) Quality.HD else Quality.SD

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = cancelAndFinish()
        })

        if (!Permissions.isGranted(this, Manifest.permission.CAMERA) ||
            !Permissions.isGranted(this, Manifest.permission.RECORD_AUDIO)
        ) {
            Toast.makeText(this, "需要相机和录音权限", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        startCamera()
    }

    override fun onStop() {
        super.onStop()
        // 切到后台时结束录制，避免后台继续占用相机
        if (isRecording) stopRecording()
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            if (isDestroyed) return@addListener
            cameraProvider = future.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    /** 绑定取景与录制用例；切换前后摄像头时重新调用 */
    private fun bindCamera() {
        val provider = cameraProvider ?: return
        val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(quality, FallbackStrategy.higherQualityOrLowerThan(Quality.SD)),
            )
            .build()
        val capture = VideoCapture.withOutput(recorder)

        provider.unbindAll()
        runCatching {
            camera = provider.bindToLifecycle(this, selector, preview, capture)
            videoCapture = capture
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(this, "无法启动照相机", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRecording() {
        val capture = videoCapture ?: return
        succeed = false
        activeRecording = capture.output
            .prepareRecording(this, FileOutputOptions.Builder(File(filePath)).build())
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(this)) { event -> onRecordEvent(event) }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        rePlayButton.visibility = View.GONE
        senderButton.visibility = View.GONE
        timeView.visibility = View.VISIBLE
        timeView.text = "00:00"
        goBackButton.visibility = View.GONE
        imgView.visibility = View.GONE
        recorderButton.setText(R.string.video_recorder_stop)
        startTimer()
    }

    private fun stopRecording() {
        activeRecording?.stop()
    }

    /** 录制事件（主线程）：Finalize 表示文件已写完，此时更新界面 */
    private fun onRecordEvent(event: VideoRecordEvent) {
        if (event !is VideoRecordEvent.Finalize || isDestroyed) return
        activeRecording = null
        timerJob?.cancel()
        timerJob = null
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        succeed = !event.hasError()
        recorderButton.setText(R.string.video_recorder_start)
        goBackButton.visibility = View.VISIBLE
        if (succeed) {
            rePlayButton.visibility = View.VISIBLE
            senderButton.visibility = View.VISIBLE
            showPreviewImage()
        } else {
            Toast.makeText(this, R.string.video_recorder_failed_to_start, Toast.LENGTH_LONG).show()
        }
    }

    /** 每秒刷新时长；到达上限时自动停止 */
    private fun startTimer() {
        timeCount = 0
        timerJob = lifecycleScope.launch {
            while (true) {
                delay(1000)
                timeCount++
                timeView.text = formatSeconds(timeCount)
                if (timeCount >= timeLimit) {
                    stopRecording()
                    break
                }
            }
        }
    }

    private fun formatSeconds(seconds: Int): String = "%02d:%02d".format(seconds / 60 % 60, seconds % 60)

    private fun showPreviewImage() {
        val retriever = MediaMetadataRetriever()
        val bitmap = try {
            retriever.setDataSource(filePath)
            retriever.getFrameAtTime(-1)
        } finally {
            retriever.release()
        }
        imgView.setImageBitmap(bitmap)
        imgView.visibility = View.VISIBLE
    }

    private fun switchCamera() {
        if (isRecording) return
        useFrontCamera = !useFrontCamera
        bindCamera()
    }

    /** 取消：停止录制（如有）并返回，不回传文件 */
    private fun cancelAndFinish() {
        if (isRecording) stopRecording()
        setResult(RESULT_CANCELED)
        finish()
    }

    /** 点击预览区域对焦 */
    private fun onPreviewTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
            camera?.cameraControl?.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
        }
        return true
    }
}
