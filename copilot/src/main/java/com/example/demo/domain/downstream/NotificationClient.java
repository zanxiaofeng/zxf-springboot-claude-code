package com.example.demo.domain.downstream;

/**
 * Client interface for the downstream notification service.
 * Responsible for sending notifications to external systems.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public interface NotificationClient {

    /**
     * Sends a user-created notification to the downstream service.
     *
     * @param userId    the ID of the newly created user
     * @param username  the username of the created user
     * @param email     the email address of the created user
     * @return true if the notification was accepted by the downstream service
     */
    boolean sendUserCreatedNotification(Long userId, String username, String email);
}
