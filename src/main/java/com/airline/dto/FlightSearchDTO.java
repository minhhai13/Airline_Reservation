package com.airline.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * FlightSearchDTO Data Transfer Object for flight search
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchDTO {

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure date is required")
    private LocalDateTime departureDate;

    @Min(value = 1, message = "At least 1 passenger required")
    private Integer passengers = 1;
}
