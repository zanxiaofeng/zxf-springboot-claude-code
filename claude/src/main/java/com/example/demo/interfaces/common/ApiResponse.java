package com.example.demo.interfaces.common;

import com.example.demo.domain.common.ErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Unified response wrapper for all API endpoints.
 * Provides consistent response structure across the application.
 *
 * @param <T> the type of response data
 * @author Demo Team
 * @since 1.0.0
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String code;
    private final T data;
    private final String message;
    private final OffsetDateTime timestamp;
    private final String traceId;
    private final List<FieldError> errors;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, String traceId) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .traceId(traceId)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, String traceId, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .traceId(traceId)
                .errors(errors)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    /**
     * Represents a field-level validation error.
     *
     * @param field         the field name
     * @param message       the error message
     * @param rejectedValue the rejected value
     */
    @Builder
    @Getter
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
