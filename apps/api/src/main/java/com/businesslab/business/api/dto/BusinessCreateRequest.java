
package com.businesslab.business.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request to create a business inside an existing cloud workspace.
 */
public record BusinessCreateRequest(

    @NotBlank(message = "Template ID is required")
    @Size(max = 100, message = "Template ID cannot exceed 100 characters")
    @Pattern(
        regexp = "^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$",
        message = "Template ID must use lowercase kebab-case"
    )
    String templateId,

    @NotBlank(message = "Template version is required")
    @Size(max = 50, message = "Template version cannot exceed 50 characters")
    String templateVersion,

    @NotBlank(message = "Business name is required")
    @Size(max = 120, message = "Business name cannot exceed 120 characters")
    String name,

    @Size(
        max = 1000,
        message = "Business description cannot exceed 1000 characters"
    )
    String description,

    @NotNull(message = "Business configuration is required")
    @Size(max = 100, message = "Business configuration has too many top-level fields")
    Map<String, Object> configuration

) {
}
