package com.ahao.wifichat.service.parcelable

import android.os.Parcelable
import com.ahao.wifichat.protocol.constant.EnumMsgType
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserMessage(
    var id: Long = 0L,
    var type: EnumMsgType? = null,
    var content: String? = null,
    var msg_time: Long = 0L,
    var sendUsername: String? = null,
) : Parcelable
