package com.airline.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * UserLoginDTO Data Transfer Object for user login
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private Boolean rememberMe = false;
}
