package com.ahao.wifichat.widget

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ahao.wifichat.R
import com.ahao.wifichat.WifiChatApplication
import com.ahao.wifichat.db.ChatMessageRow
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.ui.chat.image.ChatImagesActivity
import com.ahao.wifichat.ui.chat.video.WeixinVideoPlayerActivity
import com.ahao.wifichat.utlis.AudioPlayer
import com.ahao.wifichat.utlis.ImageUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** 单条聊天消息的气泡视图，根据消息类型选择布局 */
class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private var row: ChatMessageRow? = null
    private val scope = MainScope()

    private val audioPlayer = AudioPlayer { path ->
        if (!path.isNullOrEmpty()) updateVoiceView(false)
    }

    /** 绑定一条消息 */
    fun setMessage(message: ChatMessageRow) {
        removeAllViews()
        row = message
        val inflater = LayoutInflater.from(context)
        when (message.type) {
            DBConstants.MSG_TYPE_TXT -> {
                inflater.inflate(R.layout.watch_com_chat_msgview, this)
                    .findViewById<TextView>(R.id.tvContent).text = message.content
            }
            DBConstants.MSG_TYPE_LOCATION -> inflater.inflate(R.layout.com_chat_msg_location, this)
            DBConstants.MSG_TYPE_FILE -> bindFile(message, inflater)
        }
    }

    private fun bindFile(message: ChatMessageRow, inflater: LayoutInflater) {
        val uri = message.uri ?: return
        val filePath = if (message.dirType == DBConstants.DIR_TYPE_I) {
            uri // 自己发送的：直接使用本地路径
        } else {
            // 接收的文件保存在 应用目录/发送者用户名/文件名
            val fileName = uri.substringAfterLast('/')
            "${WifiChatApplication.instance.appPath}${File.separator}${message.otherUsername}${File.separator}$fileName"
        }

        when (message.attType) {
            DBConstants.FILE_TYPE_IMG -> {
                val view = inflater.inflate(R.layout.com_chat_msg_photo, this)
                val iv = view.findViewById<ImageView>(R.id.ivPhoto)
                Glide.with(context)
                    .load(File(filePath))
                    .placeholder(R.drawable.atach_image)
                    .override(180, 240)
                    .centerCrop()
                    .error(R.drawable.logout_btn)
                    .into(iv)
                iv.setOnClickListener {
                    context.startActivity(Intent(context, ChatImagesActivity::class.java).putExtra("url", filePath))
                }
            }
            DBConstants.FILE_TYPE_AUDIO -> {
                val tv = inflater.inflate(R.layout.com_chat_msg_voice, this).findViewById<TextView>(R.id.tvVoice)
                updateVoiceIcon(tv, false)
                tv.setOnClickListener {
                    audioPlayer.startPlay(filePath)
                    updateVoiceView(true)
                }
            }
            DBConstants.FILE_TYPE_VIDEO -> {
                val view = inflater.inflate(R.layout.com_chat_msg_video, this)
                val iv = view.findViewById<ImageView>(R.id.ivPreview)
                scope.launch {
                    val bitmap = withContext(Dispatchers.IO) { ImageUtils.getVideoBitmap(filePath, 240, 240) }
                    iv.setImageBitmap(bitmap)
                }
                iv.setOnClickListener {
                    context.startActivity(
                        Intent(context, WeixinVideoPlayerActivity::class.java).putExtra("url", filePath),
                    )
                }
            }
            DBConstants.FILE_TYPE -> inflater.inflate(R.layout.com_chat_msg_file, this)
        }
    }

    /** 语音图标：自己发送的在右侧，对方发送的在左侧；播放时显示动画 */
    fun updateVoiceIcon(tv: TextView, isPlaying: Boolean) {
        val outgoing = row?.dirType == DBConstants.DIR_TYPE_I
        val playRes = if (outgoing) R.drawable.animation_voice_play_right else R.drawable.animation_voice_play_left
        val idleRes = if (outgoing) R.drawable.voice_play_right_3 else R.drawable.voice_play_left_3
        val drawable: Drawable = resources.getDrawable(if (isPlaying) playRes else idleRes)
        if (outgoing) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        } else {
            tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }
        if (isPlaying) {
            viewTreeObserver.addOnPreDrawListener {
                (drawable as? AnimationDrawable)?.start()
                true
            }
        }
    }

    fun updateVoiceView(isPlaying: Boolean) {
        val message = row ?: return
        if (message.attType != DBConstants.FILE_TYPE_AUDIO) return
        findViewById<TextView>(R.id.tvVoice)?.let { updateVoiceIcon(it, isPlaying) }
    }

    override fun onDetachedFromWindow() {
        scope.cancel()
        super.onDetachedFromWindow()
    }
}
