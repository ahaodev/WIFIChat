package com.ahao.wifichat.utlis

import android.util.Log
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger

object NetworkUtils {

    private val LOG = Logger.getLogger(NetworkUtils::class.java.name)

    /** 得到本机 IPv4/IPv6 地址（排除回环与链路本地地址） */
    @JvmStatic
    fun getLocalIpAddress(): String? = runCatching {
        allInetAddresses().firstOrNull { !it.isLoopbackAddress && !it.isLinkLocalAddress }?.hostAddress
    }.onFailure { Log.e("MyFeiGeActivity", "获取本地IP地址失败") }.getOrNull()

    /** 得到本机地址（排除回环地址） */
    @JvmStatic
    fun getLocalInetAddress(): InetAddress? = runCatching {
        allInetAddresses().firstOrNull { !it.isLoopbackAddress }
    }.onFailure { Log.e("MyFeiGeActivity", "获取本地IP地址失败") }.getOrNull()

    private fun allInetAddresses(): Sequence<InetAddress> =
        Collections.list(NetworkInterface.getNetworkInterfaces()).asSequence()
            .flatMap { Collections.list(it.inetAddresses).asSequence() }

    fun isUsable(netif: NetworkInterface?): Boolean {
        if (netif == null) return false
        return runCatching {
            netif.isUp && !netif.isLoopback && !netif.isPointToPoint && !netif.isVirtual &&
                netif.supportsMulticast() && !netif.name.lowercase().contains("vmnet") &&
                !netif.displayName.lowercase().contains("vmnet") && hasIPv4Address(netif)
        }.onFailure { LOG.log(Level.WARNING, it.toString()) }.getOrDefault(false)
    }

    fun hasIPv4Address(netif: NetworkInterface?): Boolean =
        netif != null && Collections.list(netif.inetAddresses).any { it is Inet4Address }

    fun getIPv4Addresses(netif: NetworkInterface?): String {
        if (netif == null) return ""
        return Collections.list(netif.inetAddresses)
            .filterIsInstance<Inet4Address>()
            .joinToString("") { "${it.hostAddress} " }
    }

    fun getMacAddress(netif: NetworkInterface?): String {
        if (netif == null) return ""
        val address = runCatching { netif.hardwareAddress }
            .onFailure { LOG.log(Level.WARNING, it.toString()) }
            .getOrNull() ?: return ""
        return address.joinToString("-") { "%02x".format(it) }.uppercase()
    }

    fun getNetworkInterfaceInfo(netif: NetworkInterface?): String {
        if (netif == null) return "Invalid network interface."
        return runCatching {
            "Interface name: ${netif.displayName}\n" +
                "Device: ${netif.name}\n" +
                "Is loopback: ${netif.isLoopback}\n" +
                "Is up: ${netif.isUp}\n" +
                "Is p2p: ${netif.isPointToPoint}\n" +
                "Is virtual: ${netif.isVirtual}\n" +
                "Supports multicast: ${netif.supportsMulticast()}\n" +
                "MAC address: ${getMacAddress(netif)}\n" +
                "IP addresses: ${getIPv4Addresses(netif)}"
        }.onFailure { LOG.log(Level.WARNING, it.toString()) }
            .getOrDefault("Failed to get network interface information.")
    }

    fun getNetworkInterfaces(): List<NetworkInterface> =
        runCatching { Collections.list(NetworkInterface.getNetworkInterfaces()) }
            .onFailure { LOG.log(Level.WARNING, it.toString()) }
            .getOrDefault(emptyList())

    /** 按名称查找当前仍存在的同一网络接口 */
    fun getUpdatedNetworkInterface(origNetIf: NetworkInterface?): NetworkInterface? {
        origNetIf ?: return null
        return getNetworkInterfaces().firstOrNull { sameNetworkInterface(origNetIf, it) }
    }

    fun sameNetworkInterface(netIf1: NetworkInterface?, netIf2: NetworkInterface?): Boolean =
        netIf1 != null && netIf2 != null && netIf1.name == netIf2.name

    fun findFirstUsableNetworkInterface(): NetworkInterface? = getNetworkInterfaces().firstOrNull { isUsable(it) }

    fun getLocalHostName(): String? = runCatching { InetAddress.getLocalHost().hostName }
        .onFailure { LOG.log(Level.WARNING, it.toString()) }
        .getOrNull()

    fun getNetworkInterfaceByName(name: String?): NetworkInterface? {
        name ?: return null
        return runCatching { NetworkInterface.getByName(name) }
            .onFailure { LOG.log(Level.WARNING, it.toString()) }
            .getOrNull()
    }
}
