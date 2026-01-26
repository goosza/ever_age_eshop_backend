package com.everage.eshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zasilkovna.api")
public record ZasilkovnaConfig(
        String key,
        String url,
        String webhookSecret
) {}






