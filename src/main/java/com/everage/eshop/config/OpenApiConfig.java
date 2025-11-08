package com.everage.eshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Everage API")
                        .version("1.0")
                        .description("REST API documentation for everage eshop")
                        .contact(new Contact()
                                .name("heygosh")
                                .email("vellenore@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Eshop Application Server")
                ));
    }
}
