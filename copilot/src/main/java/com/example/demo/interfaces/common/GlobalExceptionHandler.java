package com.example.demo.interfaces.common;

import com.example.demo.domain.common.BusinessException;
import com.example.demo.domain.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

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
    private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "token", "secret", "apiKey");

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
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), traceId));
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
                        .rejectedValue(SENSITIVE_FIELDS.contains(e.getField()) ? "***" : e.getRejectedValue())
                        .build())
                .toList();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, "Request validation failed", traceId, errors));
    }

    /**
     * Handles NoSuchElementException from unchecked Optional.get() calls.
     * Returns 404 NOT_FOUND with trace ID.
     *
     * @param ex      the NoSuchElementException
     * @param request the HTTP request
     * @return 404 error response
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElement(
            NoSuchElementException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.warn("[{}] NoSuchElementException: {}", traceId, ex.getMessage());
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.NOT_FOUND, "Resource not found", traceId));
    }

    /**
     * Handles type mismatch errors (e.g. non-numeric path variable).
     *
     * @param ex      the type mismatch exception
     * @param request the HTTP request
     * @return 400 error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.warn("[{}] Type mismatch: parameter '{}' with value '{}'", traceId, ex.getName(), ex.getValue());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST,
                        "Invalid value for parameter: " + ex.getName(), traceId));
    }

    /**
     * Handles malformed request body (invalid JSON or empty body).
     *
     * @param ex      the message not readable exception
     * @param request the HTTP request
     * @return 400 error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.warn("[{}] Malformed request body: {}", traceId, ex.getMessage());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.BAD_REQUEST, "Malformed request body", traceId));
    }

    /**
     * Handles database constraint violations (e.g. duplicate unique key).
     * Maps to 409 CONFLICT for duplicate entries.
     *
     * @param ex      the data integrity violation exception
     * @param request the HTTP request
     * @return 409 error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String traceId = generateTraceId(request);
        log.warn("[{}] Data integrity violation: {}", traceId, ex.getMessage());
        return ResponseEntity.status(ErrorCode.USER_ALREADY_EXISTS.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(ApiResponse.error(ErrorCode.USER_ALREADY_EXISTS,
                        "Resource already exists", traceId));
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
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", traceId));
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
