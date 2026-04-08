package com.notcvnt.rknhardering.probe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicIpClientTest {

    @Test
    fun `extractIp strips quotes from yandex response`() {
        assertEquals("37.113.42.220", PublicIpClient.extractIp("\"37.113.42.220\""))
    }

    @Test
    fun `extractIp keeps plain ipv6 response`() {
        assertEquals("2a01:4f9:c013:d2ba::1", PublicIpClient.extractIp("2a01:4f9:c013:d2ba::1"))
    }

    @Test
    fun `extractIp rejects non ip payload`() {
        assertNull(PublicIpClient.extractIp("{\"ip\":\"37.113.42.220\"}"))
    }
}
