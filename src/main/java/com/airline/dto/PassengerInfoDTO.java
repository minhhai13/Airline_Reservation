package com.airline.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * PassengerInfoDTO Data Transfer Object for passenger information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerInfoDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Invalid phone number")
    private String phone;

    private String seatNumber;
}
