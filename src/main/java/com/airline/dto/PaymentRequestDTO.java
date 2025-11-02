package com.airline.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * PaymentRequestDTO Data Transfer Object for payment processing
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String transactionId;
}
