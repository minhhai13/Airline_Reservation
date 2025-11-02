package com.airline.exception;

/**
 * InvalidCredentialsException Thrown when authentication fails
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
