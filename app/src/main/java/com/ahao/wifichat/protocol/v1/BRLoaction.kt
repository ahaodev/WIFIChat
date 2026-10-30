package com.ahao.wifichat.protocol.v1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BRLoaction(
    var lon: Double = 0.0,
    var lat: Double = 0.0,
) : Parcelable
