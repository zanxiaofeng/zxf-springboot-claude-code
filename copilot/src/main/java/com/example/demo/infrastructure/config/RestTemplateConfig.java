package com.example.demo.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for downstream HTTP client.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with configured timeouts for downstream calls.
     *
     * @param builder the RestTemplate builder
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
