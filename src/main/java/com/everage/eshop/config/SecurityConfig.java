package com.everage.eshop.config;

import com.everage.eshop.security.HmacAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${admin.secret}")
    private String adminSecret;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - frontend catalog
                .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/collections/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/options").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/countries").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/shipping/track/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/orders/checkout").permitAll()
                // Webhooks
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers("/api/zasilkovna/webhook").permitAll()
                // Actuator health check
                .requestMatchers("/actuator/health").permitAll()
                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // All admin endpoints require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(hmacAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public HmacAuthFilter hmacAuthFilter() {
        return new HmacAuthFilter(adminSecret);
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
