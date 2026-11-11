
package com.businesslab.templates.api.dto;

import com.businesslab.templates.domain.TemplateConfigurationField;

import java.math.BigDecimal;

/**
 * Public representation of a template configuration field.
 */
public record TemplateConfigurationFieldResponse(
    String key,
    String label,
    String description,
    String type,
    boolean required,
    String example,
    BigDecimal minimum,
    BigDecimal maximum,
    Integer maxLength,
    String pattern
) {

    public static TemplateConfigurationFieldResponse from(
        TemplateConfigurationField field
    ) {
        return new TemplateConfigurationFieldResponse(
            field.key(),
            field.label(),
            field.description(),
            field.type().name(),
            field.required(),
            field.example(),
            field.minimum(),
            field.maximum(),
            field.maxLength(),
            field.pattern()
        );
    }
}
