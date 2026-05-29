package com.everage.eshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class HmacAuthFilter extends OncePerRequestFilter {

    private static final String SIGNATURE_HEADER = "X-Admin-Signature";
    private static final String TIMESTAMP_HEADER = "X-Admin-Timestamp";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    // Allow 5 minutes clock skew
    private static final long MAX_TIMESTAMP_AGE_SECONDS = 300;

    private final String adminSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply HMAC filter to admin endpoints
        return !request.getRequestURI().startsWith("/api/admin")
                && !isAdminWriteOperation(request);
    }

    private boolean isAdminWriteOperation(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        // Write operations on items/collections/shipping require admin auth
        return (method.equals("POST") || method.equals("PUT")
                || method.equals("PATCH") || method.equals("DELETE"))
                && (uri.startsWith("/api/items")
                    || uri.startsWith("/api/collections")
                    || uri.startsWith("/api/shipping"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String signature = request.getHeader(SIGNATURE_HEADER);
        String timestampStr = request.getHeader(TIMESTAMP_HEADER);

        if (signature == null || timestampStr == null) {
            log.warn("Admin request missing HMAC headers from IP: {}", request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication headers");
            return;
        }

        // Validate timestamp to prevent replay attacks
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid timestamp");
            return;
        }

        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - timestamp) > MAX_TIMESTAMP_AGE_SECONDS) {
            log.warn("Admin request with expired timestamp: {} (now: {})", timestamp, now);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Request expired");
            return;
        }

        // Build the message to sign: METHOD + PATH + TIMESTAMP
        String method = request.getMethod();
        String path = request.getRequestURI();
        String message = method + path + timestampStr;

        // Compute expected signature
        String expectedSignature;
        try {
            expectedSignature = computeHmac(adminSecret, message);
        } catch (Exception e) {
            log.error("Failed to compute HMAC: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Auth error");
            return;
        }

        // Constant-time comparison to prevent timing attacks
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            log.warn("Invalid HMAC signature for admin request: {} {}", method, path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
            return;
        }

        // Auth successful — set security context
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Admin authenticated via HMAC for: {} {}", method, path);

        filterChain.doFilter(request, response);
    }

    private String computeHmac(String secret, String message)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hmacBytes);
    }
}
