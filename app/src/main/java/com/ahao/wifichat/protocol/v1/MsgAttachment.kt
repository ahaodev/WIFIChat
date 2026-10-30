package com.ahao.wifichat.protocol.v1

import android.os.Parcelable
import com.ahao.wifichat.protocol.Fields
import kotlinx.parcelize.Parcelize

/** 附件消息体 */
@Parcelize
data class MsgAttachment(
    var uuid: String? = null,
    var type: Int = 0,
    var name: String? = null,
    var size: Long = 0L,
) : Fields(), Parcelable
