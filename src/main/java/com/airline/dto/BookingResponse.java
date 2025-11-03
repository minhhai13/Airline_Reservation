package com.airline.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private LocalDateTime bookingDate;
    private String status;
    private BigDecimal totalPrice;
    private FlightResponse flight;
    private List<PassengerInfo> passengers;
}

