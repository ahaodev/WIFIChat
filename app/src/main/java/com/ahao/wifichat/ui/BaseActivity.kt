package com.ahao.wifichat.ui

import androidx.activity.ComponentActivity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ahao.wifichat.WiFiChat

abstract class BaseActivity : ComponentActivity() {

    protected val app: WiFiChat get() = WiFiChat.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        app.activityManager.pushActivity(this)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        applySystemBarInsets()
    }

    /** 让内容避开状态栏与导航栏 */
    protected fun applySystemBarInsets() {
        val contentParent = findViewById<ViewGroup>(android.R.id.content) ?: return
        if (contentParent.childCount == 0) return
        val rootView = contentParent.getChildAt(0)
        val pl = rootView.paddingLeft
        val pt = rootView.paddingTop
        val pr = rootView.paddingRight
        val pb = rootView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(pl + insets.left, pt + insets.top, pr + insets.right, pb + insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        app.activityManager.popActivity(this)
    }

    fun startActivity(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

    fun startActivity(cls: Class<*>, key: String, bundle: Bundle) {
        startActivity(Intent(this, cls).putExtra(key, bundle))
    }

    fun startActivity(cls: Class<*>, key: String, parcelable: Parcelable) {
        startActivity(Intent(this, cls).putExtra(key, parcelable))
    }
}
