package com.notcvnt.rknhardering.checker

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.notcvnt.rknhardering.model.CategoryResult
import com.notcvnt.rknhardering.model.Finding

object DirectSignsChecker {

    private val KNOWN_PROXY_PORTS = setOf(
        "1080", "9000", "5555",   // SOCKS
        "8080", "3128",           // HTTP proxy
        "9050", "9150"            // Tor
    )

    // packageId to human-readable name
    private val KNOWN_VPN_PACKAGES = mapOf(
        "com.v2ray.ang"                         to "v2rayNG",
        "io.nekohasekai.sfa"                    to "sing-box",
        "app.hiddify.com"                       to "Hiddify",
        "com.github.metacubex.clash.meta"       to "ClashMeta for Android",
        "com.github.shadowsocks"                to "Shadowsocks",
        "com.github.shadowsocks.tv"             to "Shadowsocks TV",
        "com.happproxy"                         to "HAPP VPN",
        "io.github.saeeddev94.xray"             to "XrayNG",
        "moe.nb4a"                              to "NekoBox",
        "io.github.dovecoteescapee.byedpi"      to "ByeDPI",
        "com.romanvht.byebyedpi"                to "ByeByeDPI",
        "org.outline.android.client"            to "Outline",
        "com.psiphon3"                          to "Psiphon",
        "org.getlantern.lantern"                to "Lantern",
        "com.wireguard.android"                 to "WireGuard",
        "com.strongswan.android"                to "strongSwan",
        "org.torproject.android"                to "Tor Browser",
        "info.guardianproject.orfox"            to "Orbot",
        "org.torproject.torbrowser"             to "Tor Browser (official)",
    )

    fun check(context: Context): CategoryResult {
        val findings = mutableListOf<Finding>()

        checkVpnTransport(context, findings)
        checkSystemProxy(findings)
        checkKnownVpnApps(context, findings)

        val detected = findings.any { it.detected }
        return CategoryResult(
            name = "Прямые признаки",
            detected = detected,
            findings = findings
        )
    }

    private fun checkVpnTransport(context: Context, findings: MutableList<Finding>) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        if (activeNetwork == null) {
            findings.add(Finding("Активная сеть не найдена", false))
            return
        }

        val caps = cm.getNetworkCapabilities(activeNetwork)
        if (caps == null) {
            findings.add(Finding("NetworkCapabilities недоступны", false))
            return
        }

        val hasVpnTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        findings.add(
            Finding(
                "TRANSPORT_VPN: ${if (hasVpnTransport) "обнаружен" else "не обнаружен"}",
                hasVpnTransport
            )
        )

        val capsString = caps.toString()

        val hasIsVpn = capsString.contains("IS_VPN")
        if (hasIsVpn) {
            findings.add(Finding("Флаг IS_VPN обнаружен в capabilities", true))
        }

        val hasVpnTransportInfo = capsString.contains("VpnTransportInfo")
        if (hasVpnTransportInfo) {
            findings.add(Finding("VpnTransportInfo обнаружен в транспортной информации", true))
        }
    }

    private fun checkSystemProxy(findings: MutableList<Finding>) {
        val httpHost = System.getProperty("http.proxyHost")
        val httpPort = System.getProperty("http.proxyPort")
        val socksHost = System.getProperty("socksProxyHost")
        val socksPort = System.getProperty("socksProxyPort")

        val httpProxySet = !httpHost.isNullOrBlank()
        if (httpProxySet) {
            findings.add(Finding("HTTP прокси: $httpHost:${httpPort ?: "N/A"}", true))
            checkKnownPort(httpPort, "HTTP прокси", findings)
        } else {
            findings.add(Finding("HTTP прокси: не настроен", false))
        }

        val socksProxySet = !socksHost.isNullOrBlank()
        if (socksProxySet) {
            findings.add(Finding("SOCKS прокси: $socksHost:${socksPort ?: "N/A"}", true))
            checkKnownPort(socksPort, "SOCKS прокси", findings)
        } else {
            findings.add(Finding("SOCKS прокси: не настроен", false))
        }
    }

    private fun checkKnownVpnApps(context: Context, findings: MutableList<Finding>) {
        val pm = context.packageManager
        val installed = mutableListOf<String>()

        for ((pkg, name) in KNOWN_VPN_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                installed.add(name)
                findings.add(Finding("Установлено VPN-приложение: $name ($pkg)", true))
            } catch (_: PackageManager.NameNotFoundException) {
                // not installed
            }
        }

        if (installed.isEmpty()) {
            findings.add(Finding("Известные VPN-приложения: не обнаружены", false))
        }
    }

    private fun checkKnownPort(port: String?, type: String, findings: MutableList<Finding>) {
        if (port != null && port in KNOWN_PROXY_PORTS) {
            findings.add(Finding("$type использует известный порт $port", true))
        }
    }
}
