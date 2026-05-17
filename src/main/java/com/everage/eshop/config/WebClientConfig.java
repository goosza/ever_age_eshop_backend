package com.everage.eshop.config;

import com.stripe.Stripe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient zasilkovnaWebClient(ZasilkovnaConfig zasilkovnaConfig) {
        return WebClient.builder()
                .baseUrl(zasilkovnaConfig.getApi().getUrl())
                .defaultHeader("X-API-Key", zasilkovnaConfig.getApi().getKey())
                .defaultHeader("X-API-Password", zasilkovnaConfig.getApi().getPassword())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public StripeInitializer stripeInitializer(StripeConfig stripeConfig) {
        return new StripeInitializer(stripeConfig);
    }

    public static class StripeInitializer {
        public StripeInitializer(StripeConfig stripeConfig) {
            Stripe.apiKey = stripeConfig.key();
            // API version is managed by Stripe SDK - no need to set it globally
        }
    }
}
