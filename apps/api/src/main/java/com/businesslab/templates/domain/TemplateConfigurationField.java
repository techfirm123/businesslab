package com.businesslab.templates.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes a single configurable field exposed by a
 * business template.
 *
 * Metadata is suitable for generating dynamic UI forms.
 * Actual validation remains the responsibility of the template.
 */
public record TemplateConfigurationField(
    String key,
    String label,
    String description,
    ConfigurationValueType type,
    boolean required,
    String example,
    BigDecimal minimum,
    BigDecimal maximum,
    Integer maxLength,
    String pattern
) {

    public TemplateConfigurationField {
        Objects.requireNonNull(key, "Field key is required");
        Objects.requireNonNull(label, "Field label is required");
        Objects.requireNonNull(description, "Field description is required");
        Objects.requireNonNull(type, "Field type is required");

        key = key.trim();
        label = label.trim();
        description = description.trim();

        if (key.isBlank()) {
            throw new IllegalArgumentException(
                "Field key cannot be blank"
            );
        }

        if (label.isBlank()) {
            throw new IllegalArgumentException(
                "Field label cannot be blank"
            );
        }

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                "Field description cannot be blank"
            );
        }

        if (minimum != null && maximum != null
            && minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(
                "Minimum cannot exceed maximum"
            );
        }

        if (maxLength != null && maxLength < 1) {
            throw new IllegalArgumentException(
                "Maximum length must be positive"
            );
        }

        if (maxLength != null && type != ConfigurationValueType.STRING) {
            throw new IllegalArgumentException(
                "Maximum length is only supported for STRING fields"
            );
        }

        if ((minimum != null || maximum != null)
            && type != ConfigurationValueType.INTEGER
            && type != ConfigurationValueType.DECIMAL) {
            throw new IllegalArgumentException(
                "Numeric constraints require a numeric field type"
            );
        }

        if (pattern != null) {
            if (type != ConfigurationValueType.STRING) {
                throw new IllegalArgumentException(
                    "Patterns are only supported for STRING fields"
                );
            }

            Pattern.compile(pattern);
        }
    }
}
