package com.example.demo.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of application error codes.
 * Each code maps to a specific HTTP status and default message.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS("SUCCESS", "Success", HttpStatus.OK),
    INTERNAL_ERROR("000001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("000002", "Bad request", HttpStatus.BAD_REQUEST),
    NOT_FOUND("000003", "Resource not found", HttpStatus.NOT_FOUND),
    VALIDATION_ERROR("002001", "Validation failed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("001001", "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("001002", "User already exists", HttpStatus.CONFLICT);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
}
