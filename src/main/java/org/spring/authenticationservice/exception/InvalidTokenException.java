package org.spring.authenticationservice.exception;

/**
 * Exception thrown when an invalid, expired, or already used token is provided
 * Used for situations like account activation, password reset, or API authentication
 */
public class InvalidTokenException extends RuntimeException {


    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }


    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
