// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.service.impl;

import com.airline.dao.BookingDAO;
import com.airline.dao.PaymentDAO;
import com.airline.dto.PaymentRequestDTO;
import com.airline.entity.Booking;
import com.airline.entity.Payment;
import com.airline.service.PaymentService;
import com.airline.exception.PaymentException;
import com.airline.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PaymentServiceImpl Handles payment processing with transaction management
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentDAO paymentDAO;
    private final BookingDAO bookingDAO;

    @Autowired
    public PaymentServiceImpl(PaymentDAO paymentDAO, BookingDAO bookingDAO) {
        this.paymentDAO = paymentDAO;
        this.bookingDAO = bookingDAO;
    }

    @Override
    public Payment processPayment(PaymentRequestDTO requestDTO) {
        // 1. Validate booking exists
        Booking booking = bookingDAO.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // 2. Validate booking is in PENDING status
        if (!booking.isPending()) {
            throw new PaymentException("Only PENDING bookings can be paid");
        }

        // 3. Validate amount matches booking total
        if (booking.getTotalPrice().compareTo(requestDTO.getAmount()) != 0) {
            throw new PaymentException("Payment amount does not match booking total");
        }

        // 4. Create payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(requestDTO.getAmount())
                .paymentMethod(requestDTO.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .transactionId(requestDTO.getTransactionId())
                .build();

        return paymentDAO.save(payment);
    }

    @Override
    public Payment markPaymentSuccess(Long paymentId, String transactionId) {
        Payment payment = paymentDAO.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        Payment savedPayment = paymentDAO.save(payment);

        // Confirm booking (ATOMIC operation)
        Booking booking = payment.getBooking();
        booking.confirm();
        bookingDAO.save(booking);

        return savedPayment;
    }

    @Override
    public Payment markPaymentFailed(Long paymentId, String reason) {
        Payment payment = paymentDAO.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(Payment.PaymentStatus.FAILED);
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
}
