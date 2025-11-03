package com.airline.service.impl;

import com.airline.dao.BookingDAO;
import com.airline.dao.PaymentDAO;
import com.airline.entity.Booking;
import com.airline.entity.Payment;
import com.airline.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentDAO paymentDAO;

    @Autowired
    private BookingDAO bookingDAO;

    @Override
    public Payment createPayment(Long bookingId, BigDecimal amount, String paymentMethod) {
        Booking booking = bookingDAO.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        Payment payment = Payment.builder()
            .booking(booking)
            .amount(amount)
            .paymentMethod(paymentMethod)
            .status(Payment.PaymentStatus.PENDING)
            .build();
        
        return paymentDAO.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentDAO.findByTransactionId(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBookingId(Long bookingId) {
        return paymentDAO.findByBookingId(bookingId);
    }

    @Override
    public Payment processPayment(Long paymentId, String transactionId) {
        Payment payment = paymentDAO.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        
        // Confirm booking
        Booking booking = payment.getBooking();
        booking.confirm();
        bookingDAO.save(booking);
        
        return paymentDAO.save(payment);
    }

    @Override
    public Payment failPayment(Long paymentId, String reason) {
        Payment payment = paymentDAO.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setTransactionId("FAILED: " + reason);
        
        return paymentDAO.save(payment);
    }
}

