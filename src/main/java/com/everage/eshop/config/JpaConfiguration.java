package com.everage.eshop.config;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        value = "com.everage.eshop.repository",
        repositoryBaseClass = BaseJpaRepositoryImpl.class
)
public class JpaConfiguration {
}
