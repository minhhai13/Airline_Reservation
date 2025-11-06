package com.airline.exception;

/**
 * BookingException Thrown when booking operations fail
 */
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}
