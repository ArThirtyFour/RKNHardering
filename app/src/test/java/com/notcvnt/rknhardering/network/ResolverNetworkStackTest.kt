package com.notcvnt.rknhardering.network

import okhttp3.Dns
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class ResolverNetworkStackTest {

    @After
    fun tearDown() {
        ResolverNetworkStack.dnsFactoryOverride = null
        ResolverNetworkStack.resetForTests()
    }

    @Test
    fun `lookup bypasses configured dns for ip literals`() {
        var overrideCalls = 0
        ResolverNetworkStack.dnsFactoryOverride = { _, _ ->
            overrideCalls += 1
            object : Dns {
                override fun lookup(hostname: String) = throw UnknownHostException("NXDOMAIN")
            }
        }

        val resolved = ResolverNetworkStack.lookup(
            hostname = "149.154.167.51",
            config = DnsResolverConfig(
                mode = DnsResolverMode.DOH,
                preset = DnsResolverPreset.CUSTOM,
                customDohUrl = "https://dns.google/dns-query",
            ),
        )

        assertEquals(listOf("149.154.167.51"), resolved.mapNotNull { it.hostAddress })
        assertEquals(0, overrideCalls)
    }
}
