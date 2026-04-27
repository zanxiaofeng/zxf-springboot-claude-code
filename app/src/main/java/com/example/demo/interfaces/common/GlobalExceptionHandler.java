package com.example.demo.interfaces.common;

import com.example.demo.domain.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Catches and converts exceptions into standardized {@link ApiResponse} format.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * Handles business exceptions.
     *
     * @param ex      the business exception
     * @param request the HTTP request
     * @return error response with business error code
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.warn("[{}] Business exception: {} - {}", traceId, ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Handles parameter validation exceptions from @Valid @RequestBody.
     *
     * @param ex      the validation exception
     * @param request the HTTP request
     * @return error response with field-level validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        List<ApiResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> ApiResponse.FieldError.builder()
                        .field(e.getField())
                        .message(e.getDefaultMessage())
                        .rejectedValue(e.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, "Request validation failed", errors));
    }

    /**
     * Handles all uncaught exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return generic internal error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.error("[{}] Unexpected error: {}", traceId, ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred"));
    }

    /**
     * Generates or retrieves a trace ID for the request.
     *
     * @param request the HTTP request
     * @return the trace ID
     */
    private String generateTraceId(HttpServletRequest request) {
        String existing = request.getHeader(TRACE_ID_HEADER);
        return (existing != null && !existing.isBlank()) ? existing
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
