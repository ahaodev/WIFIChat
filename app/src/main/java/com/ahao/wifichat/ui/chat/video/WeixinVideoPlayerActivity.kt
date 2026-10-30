package com.ahao.wifichat.ui.chat.video

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.widget.FullScreenVideoView
import com.ahao.wifichat.widget.VolumnController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/** 视频播放：点击显示/隐藏控制栏，左右滑动调进度，左侧上下滑调亮度，右侧上下滑调音量 */
class WeixinVideoPlayerActivity : BaseActivity() {

    private companion object {
        const val HIDE_DELAY_MS = 5000L
    }

    private lateinit var video: FullScreenVideoView
    private lateinit var topView: View
    private lateinit var bottomView: View
    private lateinit var seekBar: SeekBar
    private lateinit var playButton: ImageView
    private lateinit var playTimeView: TextView
    private lateinit var durationView: TextView
    private lateinit var audioManager: AudioManager
    private lateinit var volumnController: VolumnController

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var threshold = 0
    private var originalLight = -1
    private var videoUrl: String = ""

    private var lastX = 0f
    private var lastY = 0f
    private var startX = 0f
    private var startY = 0f
    private var isClick = true

    private var hideJob: Job? = null
    private var progressJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        videoUrl = intent.getStringExtra("url").orEmpty()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        volumnController = VolumnController(this)
        video = findViewById(R.id.videoview)
        playTimeView = findViewById(R.id.play_time)
        durationView = findViewById(R.id.total_time)
        playButton = findViewById(R.id.play_btn)
        seekBar = findViewById(R.id.seekbar)
        topView = findViewById(R.id.top_layout)
        bottomView = findViewById(R.id.bottom_layout)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        updateScreenSize()
        threshold = DensityUtil.dip2px(this, 18f)
        originalLight = LightnessController.getLightness(this)

        playButton.setOnClickListener { togglePlay() }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) video.seekTo(progress * video.duration / 100)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                hideJob?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                scheduleHide()
            }
        })

        playVideo()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScreenSize()
    }

    override fun onPause() {
        super.onPause()
        LightnessController.setLightness(this, originalLight)
    }

    override fun onDestroy() {
        super.onDestroy()
        LightnessController.resetLightness(this)
        hideJob?.cancel()
        progressJob?.cancel()
    }

    private fun updateScreenSize() {
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val w = DensityUtil.getWidthInPx(this)
        val h = DensityUtil.getHeightInPx(this)
        screenWidth = if (landscape) h else w
        screenHeight = if (landscape) w else h
    }

    private fun playVideo() {
        video.setVideoPath(videoUrl)
        video.requestFocus()
        video.setOnPreparedListener { mp: MediaPlayer ->
            video.videoWidth = mp.videoWidth
            video.videoHeight = mp.videoHeight
            video.start()
            durationView.text = formatTime(video.duration.toLong())
            scheduleHide()
            startProgressUpdates()
        }
        video.setOnCompletionListener {
            playButton.setImageResource(R.drawable.video_btn_down)
            playTimeView.text = "00:00"
            seekBar.progress = 0
        }
        video.setOnTouchListener { _, event -> onVideoTouch(event) }
    }

    /** 每秒刷新一次播放进度与缓冲进度 */
    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = lifecycleScope.launch {
            while (true) {
                updateProgress()
                delay(1000)
            }
        }
    }

    private fun updateProgress() {
        val position = video.currentPosition
        val duration = video.duration
        if (position > 0 && duration > 0) {
            playTimeView.text = formatTime(position.toLong())
            seekBar.progress = position * 100 / duration
            seekBar.secondaryProgress = video.bufferPercentage
            if (position > duration - 100) {
                playTimeView.text = "00:00"
                seekBar.progress = 0
            }
        } else {
            playTimeView.text = "00:00"
            seekBar.progress = 0
        }
    }

    private fun togglePlay() {
        if (video.isPlaying) {
            video.pause()
            playButton.setImageResource(R.drawable.video_btn_down)
        } else {
            video.start()
            playButton.setImageResource(R.drawable.video_btn_on)
        }
    }

    private fun onVideoTouch(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                startX = x
                startY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastX
                val deltaY = y - lastY
                val absDeltaX = abs(deltaX)
                val absDeltaY = abs(deltaY)
                val adjustsLightOrVolume = when {
                    absDeltaX > threshold && absDeltaY > threshold -> absDeltaX < absDeltaY
                    absDeltaX < threshold && absDeltaY > threshold -> true
                    absDeltaX > threshold && absDeltaY < threshold -> false
                    else -> return true
                }
                if (adjustsLightOrVolume) {
                    val leftSide = x < screenWidth / 2
                    when {
                        leftSide && deltaY > 0 -> adjustLight(-absDeltaY)
                        leftSide && deltaY < 0 -> adjustLight(absDeltaY)
                        !leftSide && deltaY > 0 -> adjustVolume(-absDeltaY)
                        !leftSide && deltaY < 0 -> adjustVolume(absDeltaY)
                    }
                } else {
                    when {
                        deltaX > 0 -> seekBy(absDeltaX)
                        deltaX < 0 -> seekBy(-absDeltaX)
                    }
                }
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                if (abs(x - startX) > threshold || abs(y - startY) > threshold) isClick = false
                if (isClick) toggleControls()
                isClick = true
            }
        }
        return true
    }

    /** 按屏幕宽度比例快进/快退 */
    private fun seekBy(deltaX: Float) {
        val duration = video.duration
        if (duration <= 0) return
        val target = video.currentPosition + (deltaX / screenWidth * duration).toInt()
        video.seekTo(target)
        seekBar.progress = target * 100 / duration
        playTimeView.text = formatTime(target.toLong())
    }

    /** [delta] 为有符号增量：正数增大音量，负数减小音量 */
    private fun adjustVolume(delta: Float) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val change = (delta / screenHeight * max * 3).toInt()
        val volume = (current + change).coerceIn(0, max)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        volumnController.show(volume * 100f / max)
    }

    /** [delta] 为有符号增量：正数调亮，负数调暗 */
    private fun adjustLight(delta: Float) {
        val change = (delta / screenHeight * 255 * 3).toInt()
        LightnessController.setLightness(this, LightnessController.getLightness(this) + change)
    }

    private fun toggleControls() {
        if (topView.visibility == View.VISIBLE) hideControls() else showControls()
    }

    private fun hideControls() {
        hideJob?.cancel()
        animateOut(topView, R.anim.option_leave_from_top)
        animateOut(bottomView, R.anim.option_leave_from_bottom)
    }

    private fun showControls() {
        topView.visibility = View.VISIBLE
        topView.clearAnimation()
        topView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.option_entry_from_top))
        bottomView.visibility = View.VISIBLE
        bottomView.clearAnimation()
        bottomView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.option_entry_from_bottom))
        scheduleHide()
    }

    private fun animateOut(view: View, animRes: Int) {
        view.clearAnimation()
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, animRes)
        animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) = Unit
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) = Unit
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                view.visibility = View.GONE
            }
        })
        view.startAnimation(animation)
    }

    /** 无操作 [HIDE_DELAY_MS] 毫秒后自动隐藏控制栏 */
    private fun scheduleHide() {
        hideJob?.cancel()
        hideJob = lifecycleScope.launch {
            delay(HIDE_DELAY_MS)
            hideControls()
        }
    }

    /** mm:ss，纯数学计算，不受时区影响 */
    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        return "%02d:%02d".format(totalSeconds / 60 % 60, totalSeconds % 60)
    }
}
