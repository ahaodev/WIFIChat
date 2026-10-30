package com.ahao.wifichat.ui.me

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ahao.wifichat.R
import com.ahao.wifichat.ui.BaseFragment
import com.ahao.wifichat.ui.MainActivity

/** “我”页面 */
class PersonalCenterFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.p2p_fragment_me, null)
        initView(v)
        return v
    }

    override fun onResume() {
        super.onResume()
        val fragmentTag = tag
        (activity as MainActivity).titleView.apply {
            setTitle(fragmentTag)
            setRightVisibility(View.INVISIBLE)
        }
    }

    private fun initView(v: View) {
        v.findViewById<TextView>(R.id.text_name).text = app.me.name
        v.findViewById<TextView>(R.id.text_account).text = app.me.username
        v.findViewById<View>(R.id.btn_personal_infomation).setOnClickListener { }
        v.findViewById<View>(R.id.llsetting).setOnClickListener { startActivity(SettingActivity::class.java) }
    }
}
