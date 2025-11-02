package com.airline.exception;

/**
 * PaymentException Thrown when payment operations fail
 */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
