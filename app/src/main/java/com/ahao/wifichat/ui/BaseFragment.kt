package com.ahao.wifichat.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import com.ahao.wifichat.WiFiChat

abstract class BaseFragment : Fragment() {

    protected val app: WiFiChat get() = WiFiChat.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("ClassLoder", javaClass.`package`.toString() + javaClass.simpleName)
    }

    fun startActivity(cls: Class<*>) {
        requireActivity().startActivity(Intent(activity, cls))
    }

    fun startActivity(cls: Class<*>, key: String, bundle: Bundle) {
        requireActivity().startActivity(Intent(activity, cls).putExtra(key, bundle))
    }

    fun startActivity(cls: Class<*>, key: String, parcelable: Parcelable) {
        requireActivity().startActivity(Intent(activity, cls).putExtra(key, parcelable))
    }
}
