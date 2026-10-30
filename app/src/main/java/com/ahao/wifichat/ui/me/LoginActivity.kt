package com.ahao.wifichat.ui.me

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ahao.wifichat.AppAction
import com.ahao.wifichat.R
import com.ahao.wifichat.net.Constants
import com.ahao.wifichat.ui.BaseActivity
import com.ahao.wifichat.ui.MainActivity
import com.ahao.wifichat.utlis.NetworkUtils
import com.ahao.wifichat.utlis.PermissionRequester
import com.ahao.wifichat.utlis.Permissions
import com.ahao.wifichat.utlis.UiHelper

class LoginActivity : BaseActivity() {

    private lateinit var userEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var comfirmBtn: Button

    private val permissions = PermissionRequester(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_login)

        // Android 17+：局域网通信需要运行时授权，未授权时聊天收发将失败
        permissions.request(Permissions.startupPermissions()) { }

        val savedUser = UiHelper.getShareData(this, AppAction.KEY.USER_NUMBER, AppAction.SHREAD.USER)
        val savedName = UiHelper.getShareData(this, AppAction.KEY.USER_NAME, AppAction.SHREAD.USER)
        initView()
        if (savedName.isNotEmpty() && savedUser.isNotEmpty()) {
            userEdit.setText(savedUser)
            nameEdit.setText(savedName)
            app.me.apply {
                username = savedUser
                name = savedName
                ipAdderss = NetworkUtils.getLocalIpAddress()
                port = Constants.NETWORK_PRIVCHAT_PORT
            }
        }
    }

    private fun initView() {
        userEdit = findViewById(R.id.userid) // 用户名
        nameEdit = findViewById(R.id.passwd) // 姓名
        comfirmBtn = findViewById(R.id.comfirm)
        comfirmBtn.setOnClickListener { onConfirm() }
    }

    private fun onConfirm() {
        val userName = userEdit.text.toString()
        val name = nameEdit.text.toString()
        when {
            name.isEmpty() -> toast(R.string.error_username_empty)
            name.length > 20 -> toast(R.string.error_username_length_invalid)
            else -> {
                comfirmBtn.isEnabled = false
                app.me.apply {
                    username = userName
                    this.name = name
                    ipAdderss = NetworkUtils.getLocalIpAddress()
                    port = Constants.NETWORK_PRIVCHAT_PORT
                }
                UiHelper.setShareData(this, AppAction.SHREAD.USER, AppAction.KEY.USER_NUMBER, userName)
                UiHelper.setShareData(this, AppAction.SHREAD.USER, AppAction.KEY.USER_NAME, name)

                val manager = app.binder?.manager
                if (manager != null) {
                    manager.sendEntry()
                } else {
                    Toast.makeText(this, "ServiceBinder暂未连接", Toast.LENGTH_SHORT).show()
                }
                startActivity(MainActivity::class.java)
                finish()
            }
        }
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}
