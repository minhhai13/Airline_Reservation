package com.airline.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Entity Represents a flight route (origin -> destination)
 */
@Entity
@Table(name = "Routes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"origin", "destination"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin", nullable = false, length = 100)
    private String origin;

    @Column(name = "destination", nullable = false, length = 100)
    private String destination;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Flight> flights = new ArrayList<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateRoute();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateRoute();
    }

    // Validation
    private void validateRoute() {
        if (origin != null && origin.equalsIgnoreCase(destination)) {
            throw new IllegalArgumentException("Origin and destination cannot be the same");
        }
    }

    // Helper method
    public String getRouteDescription() {
        return origin + " → " + destination;
    }
}
