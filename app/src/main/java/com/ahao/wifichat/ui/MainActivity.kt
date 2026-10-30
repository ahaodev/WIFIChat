package com.ahao.wifichat.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TabHost.TabSpec
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTabHost
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ahao.wifichat.R
import com.ahao.wifichat.WiFiChat
import com.ahao.wifichat.db.ChatDB
import com.ahao.wifichat.ui.chatlist.ChatListFragment
import com.ahao.wifichat.ui.contacts.ContactsFragment
import com.ahao.wifichat.ui.me.PersonalCenterFragment
import com.ahao.wifichat.widget.TitleView
import kotlinx.coroutines.launch

/** 主界面：三个标签页（会话、联系人、我） */
class MainActivity : FragmentActivity() {

    private lateinit var tabHost: FragmentTabHost
    lateinit var titleView: TitleView

    private val app: WiFiChat get() = WiFiChat.instance

    private val fragmentArray = arrayOf<Class<out Fragment>>(
        ChatListFragment::class.java,
        ContactsFragment::class.java,
        PersonalCenterFragment::class.java,
    )
    private val textArray = intArrayOf(R.string.chat, R.string.contacts, R.string.me)
    private val imageArray = intArrayOf(R.drawable.selector_wechat, R.drawable.selector_contacts, R.drawable.selector_me)

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.p2p_activity_main)
        initView()
        app.activityManager.pushActivity(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ChatDB.observeUnreadTotal().collect { updateUnreadBadge(it) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_devicelist -> lifecycleScope.launch { app.binder?.manager?.sendEntry() }
            R.id.test_singout -> lifecycleScope.launch { app.binder?.manager?.sendOutLine() }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun initView() {
        titleView = findViewById(R.id.title)
        titleView.setBackVisibility(View.INVISIBLE)

        tabHost = findViewById(android.R.id.tabhost)
        tabHost.setup(this, supportFragmentManager, R.id.realtabcontent)
        val inflater = LayoutInflater.from(this)
        fragmentArray.forEachIndexed { i, fragment ->
            val tabSpec: TabSpec = tabHost.newTabSpec(getString(textArray[i])).setIndicator(getTabItemView(inflater, i))
            tabHost.addTab(tabSpec, fragment, null)
        }
    }

    @SuppressLint("InflateParams")
    private fun getTabItemView(inflater: LayoutInflater, index: Int): View {
        val view = inflater.inflate(R.layout.p2p_tab_item_view_main, null)
        view.findViewById<TextView>(R.id.textview).apply {
            setText(textArray[index])
            setCompoundDrawablesWithIntrinsicBounds(null, resources.getDrawable(imageArray[index]), null, null)
        }
        return view
    }

    private fun updateUnreadBadge(count: Int) {
        tabHost.tabWidget.getChildTabViewAt(0)?.findViewById<TextView>(R.id.tvTip)?.apply {
            visibility = if (count > 0) View.VISIBLE else View.GONE
            text = count.toString()
        }
    }
}
