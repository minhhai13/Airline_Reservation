package com.airline.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Flight Entity Represents a scheduled flight
 */
@Entity
@Table(name = "Flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true, length = 20)
    private String flightNumber;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateFlight();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateFlight();
    }

    // Validation
    private void validateFlight() {
        if (arrivalTime != null && departureTime != null
                && !arrivalTime.isAfter(departureTime)) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }
        if (availableSeats != null && availableSeats < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative");
        }
    }

    // Helper methods
    public boolean hasAvailableSeats() {
        return availableSeats != null && availableSeats > 0;
    }

    public boolean hasAvailableSeats(int requiredSeats) {
        return availableSeats != null && availableSeats >= requiredSeats;
    }

    public void decreaseAvailableSeats(int count) {
        if (!hasAvailableSeats(count)) {
            throw new IllegalStateException("Not enough available seats");
        }
        this.availableSeats -= count;
    }

    public void increaseAvailableSeats(int count) {
        this.availableSeats += count;
    }
}
