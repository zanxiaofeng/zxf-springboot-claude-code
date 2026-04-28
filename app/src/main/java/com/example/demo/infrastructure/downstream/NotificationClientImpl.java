package com.example.demo.infrastructure.downstream;

import com.example.demo.domain.downstream.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client implementation of {@link NotificationClient}.
 * Calls the downstream notification service via HTTP.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClientImpl implements NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${app.downstream.notification.base-url}")
    private String baseUrl;

    /** {@inheritDoc} */
    @Override
    public boolean sendUserCreatedNotification(Long userId, String username, String email) {
        String url = baseUrl + "/api/v1/notifications/user-created";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = Map.of(
                "userId", userId,
                "username", username,
                "email", email,
                "eventType", "USER_CREATED"
        );
        
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    Void.class
            );
            boolean accepted = response.getStatusCode().is2xxSuccessful();
            log.info("Downstream notification sent for userId={}: accepted={}", userId, accepted);
            return accepted;
        } catch (Exception e) {
            log.error("Failed to send downstream notification for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }
}
