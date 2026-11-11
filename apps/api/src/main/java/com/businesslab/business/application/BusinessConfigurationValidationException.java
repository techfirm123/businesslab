
package com.businesslab.business.application;

import com.businesslab.templates.domain.BusinessTemplateValidationError;

import java.util.List;
import java.util.Objects;

/**
 * Raised when a business configuration does not satisfy its template.
 */
public final class BusinessConfigurationValidationException
    extends RuntimeException {

    private final List<BusinessTemplateValidationError> errors;

    public BusinessConfigurationValidationException(
        List<BusinessTemplateValidationError> errors
    ) {
        super("Business configuration is invalid");

        Objects.requireNonNull(
            errors,
            "Validation errors are required"
        );

        if (errors.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one validation error is required"
            );
        }

        this.errors = List.copyOf(errors);
    }

    public List<BusinessTemplateValidationError> errors() {
        return errors;
    }
}
