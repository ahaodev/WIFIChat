package com.ahao.wifichat.net

import java.net.NetworkInterface

/** 网络接口的简单包装 */
class NetworkInterfaceInfo(val networkInterface: NetworkInterface) {
    val displayName: String get() = networkInterface.displayName
    val name: String get() = networkInterface.name
}
