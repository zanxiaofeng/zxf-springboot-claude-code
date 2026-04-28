package com.example.demo.application.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user.
 * Contains validation constraints for all fields.
 *
 * @param username the username, 3-20 alphanumeric characters
 * @param email    the email address, must be valid format
 * @param password the password, min 8 chars with at least 1 uppercase, 1 number, 1 special char
 * @author Demo Team
 * @since 1.0.0
 */
public record CreateUserRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Username must be alphanumeric (dots and underscores allowed)")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).+$",
                message = "Password must contain at least 1 uppercase, 1 number, 1 special character")
        String password
) {
}
