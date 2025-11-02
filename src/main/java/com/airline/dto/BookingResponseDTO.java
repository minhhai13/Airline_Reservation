package com.airline.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BookingResponseDTO Data Transfer Object for booking response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long bookingId;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String bookingStatus;
    private BigDecimal totalPrice;
    private Integer passengerCount;
    private LocalDateTime bookingDate;
}
