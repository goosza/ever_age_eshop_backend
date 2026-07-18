package com.everage.eshop.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    // Buckets per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Checkout: max 5 requests per minute per IP
    private static final int CHECKOUT_LIMIT = 5;
    private static final Duration CHECKOUT_WINDOW = Duration.ofMinutes(1);

    // Order tracking: low limit to make order number brute-forcing impractical
    private static final int ORDER_TRACKING_LIMIT = 10;
    private static final Duration ORDER_TRACKING_WINDOW = Duration.ofMinutes(1);

    // General public API: max 100 requests per minute per IP
    private static final int GENERAL_LIMIT = 100;
    private static final Duration GENERAL_WINDOW = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = getClientIp(request);
        String uri = request.getRequestURI();

        Bucket bucket = getBucket(ip, uri);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {}", ip, uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Please slow down."}
                    """);
        }
    }

    private Bucket getBucket(String ip, String uri) {
        String bucketType = resolveBucketType(uri);
        String key = ip + ":" + bucketType;

        return buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(bandwidthFor(bucketType)).build());
    }

    private String resolveBucketType(String uri) {
        if (uri.contains("/orders/checkout")) {
            return "checkout";
        }
        // Covers /api/orders/track/** and /api/orders/by-session/** — both let a caller
        // guess an identifier, so they get a stricter limit than the general public API.
        if (uri.contains("/orders/track/") || uri.contains("/orders/by-session/")) {
            return "order-tracking";
        }
        return "general";
    }

    private Bandwidth bandwidthFor(String bucketType) {
        return switch (bucketType) {
            case "checkout" -> Bandwidth.builder()
                    .capacity(CHECKOUT_LIMIT)
                    .refillGreedy(CHECKOUT_LIMIT, CHECKOUT_WINDOW)
                    .build();
            case "order-tracking" -> Bandwidth.builder()
                    .capacity(ORDER_TRACKING_LIMIT)
                    .refillGreedy(ORDER_TRACKING_LIMIT, ORDER_TRACKING_WINDOW)
                    .build();
            default -> Bandwidth.builder()
                    .capacity(GENERAL_LIMIT)
                    .refillGreedy(GENERAL_LIMIT, GENERAL_WINDOW)
                    .build();
        };
    }

    private String getClientIp(HttpServletRequest request) {
        // Check for proxy headers first
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip rate limiting for admin (HMAC auth is sufficient)
        // Skip for webhooks (trusted sources)
        // Skip for actuator health
        return uri.startsWith("/api/admin")
                || uri.startsWith("/api/webhooks")
                || uri.startsWith("/api/zasilkovna/webhook")
                || uri.startsWith("/actuator");
    }
}
