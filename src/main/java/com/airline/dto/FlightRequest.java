package com.airline.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FlightRequest {

    private String flightNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private Integer availableSeats;
    private Long routeId;
    private Long aircraftId;
}
