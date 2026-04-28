package com.example.demo.interfaces.common;

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

    @Builder.Default
    private final OffsetDateTime timestamp = OffsetDateTime.now();

    private final String traceId;
    private final List<FieldError> errors;

    /**
     * Creates a success response with data.
     *
     * @param data the response data
     * @param <T>  the data type
     * @return success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .data(data)
                .build();
    }

    /**
     * Creates an error response with the given error code and message.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @param <T>       the data type
     * @return error ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    /**
     * Creates an error response with field validation errors.
     *
     * @param errorCode the error code
     * @param message   the error message
     * @param errors    the list of field errors
     * @param <T>       the data type
     * @return error ApiResponse with field errors
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .errors(errors)
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
