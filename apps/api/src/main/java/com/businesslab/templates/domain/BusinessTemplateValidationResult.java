package com.businesslab.templates.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result of validating configuration against a business template.
 */
public record BusinessTemplateValidationResult(
    List<BusinessTemplateValidationError> errors
) {

    public BusinessTemplateValidationResult {
        Objects.requireNonNull(
            errors,
            "Validation errors cannot be null"
        );

        errors = List.copyOf(errors);
    }

    public static BusinessTemplateValidationResult valid() {
        return new BusinessTemplateValidationResult(List.of());
    }

    public static BusinessTemplateValidationResult invalid(
        List<BusinessTemplateValidationError> errors
    ) {
        Objects.requireNonNull(errors, "Validation errors are required");

        if (errors.isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid validation result must contain at least one error"
            );
        }

        return new BusinessTemplateValidationResult(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
