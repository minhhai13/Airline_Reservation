package com.airline.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    private String origin;
    private String destination;
    private LocalDate date;
    private Integer passengers = 1;
}

