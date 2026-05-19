package com.everage.eshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zasilkovna")
@Data
public class ZasilkovnaConfig {
    private Api api;
    private Sender sender;
    private Sandbox sandbox;
    
    @Data
    public static class Api {
        private String url;
        private String key;
        private String password;
    }
    
    @Data
    public static class Sender {
        private String id;
        private String name;
        private String email;
        private String phone;
    }
    
    @Data
    public static class Sandbox {
        private boolean enabled;
    }
}






