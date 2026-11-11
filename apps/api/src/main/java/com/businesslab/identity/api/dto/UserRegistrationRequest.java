
package com.businesslab.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address has an invalid format")
    @Size(max = 320, message = "Email address cannot exceed 320 characters")
    String email,

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    String displayName,

    @NotBlank(message = "Password is required")
    @Size(
        min = 12,
        max = 72,
        message = "Password must contain between 12 and 72 characters"
    )
    String password
) {
}
