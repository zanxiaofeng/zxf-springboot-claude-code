package com.example.demo.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for HTTP clients used to communicate with downstream services.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a {@link RestTemplate} with reasonable timeouts for downstream calls.
     *
     * @param builder the Spring Boot RestTemplateBuilder
     * @return configured RestTemplate
     */
    @Bean
    public RestTemplate downstreamRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
