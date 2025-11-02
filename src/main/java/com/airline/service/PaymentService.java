package com.airline.service;

import com.airline.dto.PaymentRequestDTO;
import com.airline.entity.Payment;
import java.util.List;
import java.util.Optional;

/**
 * PaymentService Interface
 */
public interface PaymentService {

    Payment processPayment(PaymentRequestDTO requestDTO);

    Payment markPaymentSuccess(Long paymentId, String transactionId);

    Payment markPaymentFailed(Long paymentId, String reason);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingId(Long bookingId);
}
