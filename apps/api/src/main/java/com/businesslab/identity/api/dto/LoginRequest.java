
package com.businesslab.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address has an invalid format")
    @Size(max = 320, message = "Email address cannot exceed 320 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(max = 72, message = "Password cannot exceed 72 characters")
    String password
) {
}
