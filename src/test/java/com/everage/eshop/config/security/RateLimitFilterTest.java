package com.everage.eshop.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        filterChain = Mockito.mock(FilterChain.class);
    }

    // ── shouldNotFilter ──────────────────────────────────────────────────────

    @Test
    void shouldNotFilter_forAdminEndpoints() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/orders");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_forWebhookEndpoints() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/webhooks/stripe");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_forZasilkovnaWebhook() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/zasilkovna/webhook");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_forActuator() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldFilter_forPublicOrderTrackingEndpoints() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/track/EVE-2026-ABCDEFGHJK");
        assertTrue(!filter.shouldNotFilter(request));
    }

    // ── bucket classification via observed behavior ─────────────────────────
    // We can't call the private resolveBucketType/bandwidthFor directly, so we
    // verify classification indirectly: send N+1 requests and check exactly when
    // the 429 kicks in, for each endpoint category.

    @Test
    void orderTracking_allowsUpToTenRequestsPerMinutePerIp() throws Exception {
        for (int i = 1; i <= 10; i++) {
            MockHttpServletResponse response = doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "1.2.3.4");
            assertEquals(HttpStatus.OK.value(), response.getStatus(), "Request #" + i + " should be allowed");
        }
        verify(filterChain, times(10)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void orderTracking_blocksTheEleventhRequestWithinTheSameMinute() throws Exception {
        for (int i = 1; i <= 10; i++) {
            doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "1.2.3.5");
        }

        MockHttpServletResponse eleventh = doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "1.2.3.5");

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), eleventh.getStatus());
        verify(filterChain, times(10)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void orderBySession_sharesTheSameStrictLimitAsOrderTracking() throws Exception {
        // Both endpoints let a caller guess an identifier, so they must share one bucket
        // per IP rather than each getting their own 10 requests.
        for (int i = 1; i <= 5; i++) {
            doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "1.2.3.6");
        }
        for (int i = 1; i <= 5; i++) {
            doRequest("/api/orders/by-session/cs_test_123", "1.2.3.6");
        }

        MockHttpServletResponse eleventh = doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "1.2.3.6");

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), eleventh.getStatus());
    }

    @Test
    void checkout_allowsUpToFiveRequestsPerMinutePerIp() throws Exception {
        for (int i = 1; i <= 5; i++) {
            MockHttpServletResponse response = doRequest("/api/orders/checkout", "2.2.2.2");
            assertEquals(HttpStatus.OK.value(), response.getStatus(), "Request #" + i + " should be allowed");
        }

        MockHttpServletResponse sixth = doRequest("/api/orders/checkout", "2.2.2.2");
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), sixth.getStatus());
    }

    @Test
    void checkout_doesNotShareABucketWithOrderTracking() throws Exception {
        // Exhaust the checkout bucket...
        for (int i = 1; i <= 5; i++) {
            doRequest("/api/orders/checkout", "3.3.3.3");
        }
        // ...tracking from the same IP must still be allowed since it's a different bucket.
        MockHttpServletResponse trackingResponse = doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "3.3.3.3");
        assertEquals(HttpStatus.OK.value(), trackingResponse.getStatus());
    }

    @Test
    void generalEndpoints_allowManyMoreRequestsThanOrderTracking() throws Exception {
        // 11 requests would already be blocked for order-tracking, but general
        // catalog endpoints (e.g. /api/items) should still pass at this volume.
        for (int i = 1; i <= 11; i++) {
            MockHttpServletResponse response = doRequest("/api/items/all", "4.4.4.4");
            assertEquals(HttpStatus.OK.value(), response.getStatus(), "Request #" + i + " should be allowed");
        }
    }

    @Test
    void differentIps_getIndependentBuckets() throws Exception {
        for (int i = 1; i <= 10; i++) {
            doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "5.5.5.5");
        }
        // A different IP must not be affected by IP 5.5.5.5 exhausting its bucket.
        MockHttpServletResponse response = doRequest("/api/orders/track/EVE-2026-ABCDEFGHJK", "6.6.6.6");
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    void blockedRequest_neverReachesTheFilterChain() throws Exception {
        for (int i = 1; i <= 5; i++) {
            doRequest("/api/orders/checkout", "7.7.7.7");
        }
        Mockito.clearInvocations(filterChain);

        doRequest("/api/orders/checkout", "7.7.7.7");

        verify(filterChain, never()).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void blockedRequest_returnsJsonErrorBody() throws Exception {
        for (int i = 1; i <= 5; i++) {
            doRequest("/api/orders/checkout", "8.8.8.8");
        }

        MockHttpServletResponse response = doRequest("/api/orders/checkout", "8.8.8.8");

        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Too Many Requests"));
    }

    @Test
    void clientIp_prefersXForwardedForHeaderOverRemoteAddr() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/checkout");
        request.addHeader("X-Forwarded-For", "9.9.9.9, 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Exhaust the bucket for forwarded IP 9.9.9.9 by making 5 requests through it.
        for (int i = 1; i <= 5; i++) {
            MockHttpServletRequest r = new MockHttpServletRequest("GET", "/api/orders/checkout");
            r.addHeader("X-Forwarded-For", "9.9.9.9, 10.0.0.1");
            r.setRemoteAddr("127.0.0.1");
            filter.doFilterInternal(r, new MockHttpServletResponse(), filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());
    }

    private MockHttpServletResponse doRequest(String uri, String remoteAddr) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRemoteAddr(remoteAddr);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.OK.value());

        filter.doFilterInternal(request, response, filterChain);

        return response;
    }
}
