package com.ahao.wifichat.utlis

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/** 运行时权限集合（按系统版本区分） */
object Permissions {

    /** Android 17（API 37）起访问局域网设备需要 ACCESS_LOCAL_NETWORK */
    const val ANDROID_17_SDK = Build.VERSION_CODES.CINNAMON_BUN

    /** 启动时需要的权限：局域网访问（仅 Android 17+） */
    fun startupPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= ANDROID_17_SDK) arrayOf(Manifest.permission.ACCESS_LOCAL_NETWORK) else emptyArray()

    /** 读取相册/视频所需的权限：Android 13+ 使用细分的媒体权限 */
    fun mediaReadPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun isGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * 基于系统 Activity Result API 的权限申请器。
 * 必须在 Activity 初始化阶段（属性初始化时）创建，否则 registerForActivityResult 会失败。
 */
class PermissionRequester(private val activity: ComponentActivity) {

    private var pending: ((Boolean) -> Unit)? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val callback = pending
        pending = null
        callback?.invoke(results.values.all { it })
    }

    /** 仅申请尚未授予的权限；全部已授予时直接回调 true */
    fun request(permissions: Array<String>, onResult: (granted: Boolean) -> Unit) {
        val missing = permissions.filter { !Permissions.isGranted(activity, it) }
        if (missing.isEmpty()) {
            onResult(true)
            return
        }
        pending = onResult
        launcher.launch(missing.toTypedArray())
    }
}
