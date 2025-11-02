package com.airline.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking Entity Represents a booking/reservation made by a user
 */
@Entity
@Table(name = "Bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingPassenger> passengers = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    // Helper methods
    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }

    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be confirmed");
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public int getPassengerCount() {
        return passengers != null ? passengers.size() : 0;
    }

    // Helper method to add passenger
    public void addPassenger(BookingPassenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }
}
