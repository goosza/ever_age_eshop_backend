package com.everage.eshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String accessKey,
        String secretKey,
        String endpoint,
        String bucket,
        String publicUrl
) {}
