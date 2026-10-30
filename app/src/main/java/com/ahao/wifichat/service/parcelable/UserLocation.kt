package com.ahao.wifichat.service.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLocation(
    var username: String? = null,
    var name: String? = null,
    var lon: Double = 0.0,
    var lat: Double = 0.0,
) : Parcelable
