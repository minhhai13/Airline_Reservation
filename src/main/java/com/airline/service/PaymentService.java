package com.airline.service;

import com.airline.entity.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Payment createPayment(Long bookingId, BigDecimal amount, String paymentMethod);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingId(Long bookingId);

    Payment createPayment(Long bookingId, BigDecimal amount, String paymentMethod, String transactionId);

    Payment processPayment(Long paymentId, String transactionId);

    Payment failPayment(Long paymentId, String reason);
}
