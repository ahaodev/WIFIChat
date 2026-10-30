package com.ahao.wifichat.protocol.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** 上线通知内容 */
@Parcelize
data class Entry(
    var name: String? = null,
    var ipAdderss: String? = null,
) : Parcelable
