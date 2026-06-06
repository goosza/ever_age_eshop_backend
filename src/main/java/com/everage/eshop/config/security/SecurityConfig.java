package com.everage.eshop.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${admin.secret}")
    private String adminSecret;

    @Value("${admin.dev-mode:false}")
    private boolean adminDevMode;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - frontend catalog
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/collections/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/options").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/countries").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/track/**").permitAll()
                // Order tracking — only by session ID or order number (known only to the buyer)
                .requestMatchers(HttpMethod.GET, "/api/orders/by-session/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/track/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/orders/checkout").permitAll()
                // Webhooks
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers("/api/zasilkovna/webhook").permitAll()
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                // Swagger — only when enabled (dev/local)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").access(
                    (authentication, ctx) -> new AuthorizationDecision(swaggerEnabled)
                )
                // All admin endpoints require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(hmacAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public HmacAuthFilter hmacAuthFilter() {
        return new HmacAuthFilter(adminSecret, adminDevMode);
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    /**
     * Disable Spring Security's default UserDetailsService and auto-generated password.
     * We use HMAC-based authentication, not username/password.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Return empty service — no users needed, auth is done via HMAC filter
        return username -> {
            throw new UsernameNotFoundException("No users");
        };
    }

    /**
     * 401 - not authenticated (missing/invalid HMAC headers)
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Authentication required: provide X-Admin-Signature and X-Admin-Timestamp headers"
            )));
        };
    }

    /**
     * 403 - authenticated but not authorized (wrong role)
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", 403,
                    "error", "Forbidden",
                    "message", "Access denied: admin privileges required"
            )));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
