package com.ahao.wifichat.protocol.v1

import android.os.Parcelable
import com.ahao.wifichat.protocol.Fields
import kotlinx.parcelize.Parcelize

/** 文本消息体 */
@Parcelize
data class MsgText(
    var text: String? = null,
) : Fields(), Parcelable
