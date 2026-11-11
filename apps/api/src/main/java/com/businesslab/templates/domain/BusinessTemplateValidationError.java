package com.businesslab.templates.domain;

import java.util.Objects;

/**
 * Represents a template-specific configuration validation error.
 */
public record BusinessTemplateValidationError(
    String fieldKey,
    String code,
    String message
) {

    public BusinessTemplateValidationError {
        Objects.requireNonNull(
            fieldKey,
            "Validation field key is required"
        );

        Objects.requireNonNull(
            code,
            "Validation error code is required"
        );

        Objects.requireNonNull(
            message,
            "Validation error message is required"
        );

        if (fieldKey.isBlank()) {
            throw new IllegalArgumentException(
                "Validation field key cannot be blank"
            );
        }

        if (code.isBlank()) {
            throw new IllegalArgumentException(
                "Validation error code cannot be blank"
            );
        }

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                "Validation error message cannot be blank"
            );
        }
    }
}
