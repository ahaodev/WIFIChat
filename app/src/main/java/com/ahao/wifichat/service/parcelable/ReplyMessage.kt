package com.ahao.wifichat.service.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReplyMessage(
    var cmd: Int = 0,
    var packet_no: Int = 0,
    var msg_id: Int = 0,
) : Parcelable
