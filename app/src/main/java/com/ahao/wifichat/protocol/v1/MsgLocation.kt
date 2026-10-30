package com.ahao.wifichat.protocol.v1

import android.os.Parcelable
import com.ahao.wifichat.protocol.Fields
import kotlinx.parcelize.Parcelize

/** 位置消息体 */
@Parcelize
data class MsgLocation(
    var name: String? = null,
    var lon: Double = 0.0,
    var lat: Double = 0.0,
) : Fields(), Parcelable
