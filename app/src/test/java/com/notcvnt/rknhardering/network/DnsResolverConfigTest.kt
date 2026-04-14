package com.notcvnt.rknhardering.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DnsResolverConfigTest {

    @Test
    fun `cloudflare direct preset exposes expected servers`() {
        val config = DnsResolverConfig(
            mode = DnsResolverMode.DIRECT,
            preset = DnsResolverPreset.CLOUDFLARE,
        )

        assertEquals(listOf("1.1.1.1", "1.0.0.1"), config.effectiveDirectServers())
        assertNull(config.effectiveDohUrl())
    }

    @Test
    fun `yandex doh preset exposes expected endpoint and bootstrap hosts`() {
        val config = DnsResolverConfig(
            mode = DnsResolverMode.DOH,
            preset = DnsResolverPreset.YANDEX,
        )

        assertEquals("https://common.dot.dns.yandex.net/dns-query", config.effectiveDohUrl())
        assertEquals(listOf("77.88.8.8", "77.88.8.1"), config.effectiveDohBootstrapHosts())
    }

    @Test
    fun `invalid custom doh config falls back to system mode`() {
        val config = DnsResolverConfig(
            mode = DnsResolverMode.DOH,
            preset = DnsResolverPreset.CUSTOM,
            customDohUrl = "http://not-valid.example",
        ).sanitized()

        assertEquals(DnsResolverMode.SYSTEM, config.mode)
        assertEquals(emptyList<String>(), config.effectiveDirectServers())
        assertNull(config.effectiveDohUrl())
    }

    @Test
    fun `invalid custom direct servers fall back to system mode`() {
        val config = DnsResolverConfig(
            mode = DnsResolverMode.DIRECT,
            preset = DnsResolverPreset.CUSTOM,
            customDirectServers = listOf("not-an-ip", "999.999.999.999"),
        ).sanitized()

        assertEquals(DnsResolverMode.SYSTEM, config.mode)
        assertEquals(emptyList<String>(), config.effectiveDirectServers())
    }

    @Test
    fun `custom direct servers keep only valid ip literals`() {
        val config = DnsResolverConfig(
            mode = DnsResolverMode.DIRECT,
            preset = DnsResolverPreset.CUSTOM,
            customDirectServers = listOf("1.1.1.1", "bad-host", "2001:4860:4860::8888"),
        )

        assertEquals(listOf("1.1.1.1", "2001:4860:4860::8888"), config.effectiveDirectServers())
    }

    @Test
    fun `hostnames are rejected as direct dns literals`() {
        assertEquals(false, DnsResolverConfig.isValidIpLiteral("dns.google"))
    }

    @Test
    fun `address list parser trims and deduplicates entries`() {
        val values = DnsResolverConfig.parseAddressList("1.1.1.1, 1.0.0.1  1.1.1.1\n8.8.8.8")

        assertEquals(listOf("1.1.1.1", "1.0.0.1", "8.8.8.8"), values)
    }
}
