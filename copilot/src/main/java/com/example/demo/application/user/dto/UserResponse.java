package com.example.demo.application.user.dto;

import com.example.demo.domain.user.UserStatus;

import java.time.OffsetDateTime;

/**
 * Response DTO for user data.
 * Excludes sensitive fields like password.
 *
 * @param id        the user ID
 * @param username  the username
 * @param email     the email address
 * @param status    the account status
 * @param createdAt the creation timestamp
 * @author Demo Team
 * @since 1.0.0
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        UserStatus status,
        String bio,
        OffsetDateTime createdAt
) {
}
