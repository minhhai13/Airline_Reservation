// ========================================
// PaymentDAO
// ========================================
package com.airline.dao;

import com.airline.entity.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentDAO {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findAll();

    void delete(Payment payment);
}
