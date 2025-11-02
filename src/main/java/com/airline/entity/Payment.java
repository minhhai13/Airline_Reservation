package com.airline.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity Represents a payment transaction for a booking
 */
@Entity
@Table(name = "Payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }

    // Enums
    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    // Helper methods
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
}
