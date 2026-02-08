package com.everage.eshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe.api")
public record StripeConfig(String key) {}
