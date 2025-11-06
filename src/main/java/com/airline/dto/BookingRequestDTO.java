package com.airline.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingRequestDTO Data Transfer Object for creating a booking
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    @Builder.Default
    private List<PassengerInfoDTO> passengers = new ArrayList<>();

}
