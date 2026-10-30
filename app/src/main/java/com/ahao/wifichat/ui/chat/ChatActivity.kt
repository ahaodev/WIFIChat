package com.ahao.wifichat.ui.chat

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ahao.wifichat.AppConfig
import com.ahao.wifichat.R
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.db.DBConstants
import com.ahao.wifichat.net.file.TcpClient
import com.ahao.wifichat.service.ChatEvent
import com.ahao.wifichat.service.ChatEvents
import com.ahao.wifichat.service.parcelable.OnlineUserInfo
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.ui.chat.video.VideoRecorderActivity
import com.ahao.wifichat.utlis.ImageUtils
import com.ahao.wifichat.utlis.PermissionRequester
import com.ahao.wifichat.utlis.Permissions
import com.ahao.wifichat.utlis.StorageUntils
import com.ahao.wifichat.widget.ChatInputView
import com.ahao.wifichat.widget.TitleView
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 与某个用户的聊天界面 */
class ChatActivity : BaseActivity(), ChatInputView.InputClickListener {

    companion object {
        const val USER = "user"
        private const val REQUEST_CODE_CHOOSE = 23
        private const val TAG = "ChatActivity"
    }

    private lateinit var user: OnlineUserInfo
    private val peer: String get() = user.userName

    private lateinit var chatInputView: ChatInputView
    private lateinit var chatListView: ListView
    private lateinit var adapter: ChatAdapter

    private val permissions = PermissionRequester(this)

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendFileFromPath(ImageUtils.getRealPathFromURI(this, it)) }
    }

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendVideoFile(ImageUtils.getRealFilePath(this, it)) }
    }

    private val recordVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.action?.let { sendVideoFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_chat)

        val peerInfo = BundleCompat.getParcelable(intent.extras ?: Bundle(), USER, OnlineUserInfo::class.java)
        if (peerInfo == null) {
            finish()
            return
        }
        user = peerInfo

        findViewById<TitleView>(R.id.title).setTitle(user.name)
        chatInputView = findViewById(R.id.chatInput)
        if (user.online == 0) chatInputView.visibility = View.GONE
        chatInputView.inputClickListener = this
        chatInputView.audioCallback = ChatAudioListener(this)
        chatInputView.onAdditionExpanded = { smoothScroll(adapter.count - 1) }
        chatInputView.onRequestRecordPermission = {
            permissions.request(arrayOf(android.Manifest.permission.RECORD_AUDIO)) { }
        }

        findViewById<View>(R.id.chat_windo).setOnClickListener {
            chatInputView.hideAdditionPanel()
            chatInputView.hideKeyboard()
        }

        chatListView = findViewById(R.id.p2p_chat_messagelist)
        adapter = ChatAdapter(this, emptyList())
        chatListView.adapter = adapter
        registerForContextMenu(chatInputView)

        lifecycleScope.launch {
            ChatDB.updateReadStatus(peer)
            ChatDB.updateSendTimeOut(peer, System.currentTimeMillis() - 10 * 1000)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    ChatDB.observeConversation(peer).collect { rows -> adapter.notifyDataSetChanged(rows) }
                }
                launch {
                    ChatEvents.events.collect { event -> onChatEvent(event) }
                }
            }
        }
    }

    private fun onChatEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.OnlineUserUpdate -> {
                if (event.user.userName != peer) return
                if (app.binder?.getUser(peer) == null) {
                    Toast.makeText(this, "对方已经下线", Toast.LENGTH_SHORT).show()
                    chatInputView.visibility = View.GONE
                } else {
                    chatInputView.visibility = View.VISIBLE
                    Toast.makeText(this, "对方已经上线", Toast.LENGTH_SHORT).show()
                }
            }
            is ChatEvent.NewMessage -> {
                if (event.msg.sendUsername != peer) return
                smoothScroll(adapter.count - 1)
                lifecycleScope.launch { ChatDB.updateReadStatus(peer) }
            }
            is ChatEvent.LocationUpdate -> Unit
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.contextmenu_chat_vedio, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_vedio_tack -> recordVideo()
            R.id.menu_vedio_choose -> pickVideo.launch("video/*")
        }
        return super.onContextItemSelected(item)
    }

    /** 录像：需要存储、相机、录音权限 */
    private fun recordVideo() {
        // 文件保存在应用私有目录，无需外部存储写权限
        if (!StorageUntils.hasStorage(false)) {
            Toast.makeText(this, R.string.insert_sd_card, Toast.LENGTH_LONG).show()
            return
        }
        val path = createVideoTmp()
        permissions.request(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)) { granted ->
            if (!granted) {
                Toast.makeText(this, "需要相机和录音权限", Toast.LENGTH_SHORT).show()
                return@request
            }
            AppConfig.videoPath = path
            val intent = Intent(this, VideoRecorderActivity::class.java)
                .putExtra(VideoRecorderActivity.VIDEO_QUALITY, 0)
                .putExtra(VideoRecorderActivity.VIDEO_TIME_LIMIT, 30)
                .putExtra(VideoRecorderActivity.VIDEO_FILEPATH, Uri.fromFile(File(path)).path)
            try {
                recordVideoLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "摄像头异常", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createVideoTmp(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${getExternalFilesDir("VideoFile")}/VID_$timeStamp.mp4"
    }

    override fun sendImage() {
        permissions.request(Permissions.mediaReadPermissions()) { granted ->
            if (granted) {
                Matisse.from(this)
                    .choose(MimeType.ofImage())
                    .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                    .countable(false)
                    .maxSelectable(9)
                    .imageEngine(GlideEngine())
                    .forResult(REQUEST_CODE_CHOOSE)
            } else {
                Toast.makeText(this, "需要读取相册权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun sendVideo() {
        // 弹出录制/选择的上下文菜单
        chatInputView.showContextMenuForChild(chatInputView)
    }

    override fun sendLocation() {
        // 位置消息暂未开放
    }

    override fun sendFile() {
        pickFile.launch("*/*")
    }

    override fun sendMessage(message: String) {
        lifecycleScope.launch {
            val sent = app.binder?.manager?.sendText(user, message) ?: false
            Log.d(TAG, "发送情况: $sent")
            smoothScroll(adapter.count - 1)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            Matisse.obtainResult(data).orEmpty().forEach { uri ->
                sendLocalFile(ImageUtils.getRealFilePath(this, uri), DBConstants.FILE_TYPE_IMG)
            }
        }
    }

    /** 语音录制完成 */
    fun sendAudio(path: String?) = sendLocalFile(path, DBConstants.FILE_TYPE_AUDIO)

    private fun sendVideoFile(path: String?) = sendLocalFile(path, DBConstants.FILE_TYPE_VIDEO)

    private fun sendFileFromPath(path: String?) = sendLocalFile(path, DBConstants.FILE_TYPE)

    private fun sendLocalFile(path: String?, type: Int) {
        if (path.isNullOrEmpty()) return
        TcpClient.sendFile(path, user, type)
        chatInputView.hideAdditionPanel()
    }

    private fun smoothScroll(position: Int) {
        chatListView.post { chatListView.smoothScrollToPosition(position) }
    }
}
