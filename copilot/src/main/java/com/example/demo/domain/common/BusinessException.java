package com.example.demo.domain.common;


/**
 * Base exception for all business logic errors.
 * Carries an {@link ErrorCode} to determine the appropriate HTTP response.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    /**
     * Creates a new BusinessException with the given error code and arguments.
     *
     * @param errorCode the error code representing the business error
     * @param args optional arguments for message formatting
     */
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    /** @return the error code of this exception. */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** @return the formatting arguments. */
    public Object[] getArgs() {
        return args;
    }
}
