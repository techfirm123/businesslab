package com.businesslab.business.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identifies the exact business template version used
 * to create a business.
 *
 * Template identity is independent of the Business aggregate.
 */
public record BusinessTemplateReference(
    String templateId,
    String version
) {

    private static final Pattern TEMPLATE_ID_PATTERN =
        Pattern.compile("[a-z][a-z0-9]*(?:-[a-z0-9]+)*");

    private static final int MAX_TEMPLATE_ID_LENGTH = 100;

    private static final int MAX_VERSION_LENGTH = 50;

    public BusinessTemplateReference {
        Objects.requireNonNull(templateId, "Template ID is required");
        Objects.requireNonNull(version, "Template version is required");

        templateId = templateId.trim();
        version = version.trim();

        if (templateId.isBlank()) {
            throw new IllegalArgumentException(
                "Template ID cannot be blank"
            );
        }

        if (templateId.length() > MAX_TEMPLATE_ID_LENGTH) {
            throw new IllegalArgumentException(
                "Template ID exceeds maximum length"
            );
        }

        if (!TEMPLATE_ID_PATTERN.matcher(templateId).matches()) {
            throw new IllegalArgumentException(
                "Template ID must use lowercase kebab-case"
            );
        }

        if (version.isBlank()) {
            throw new IllegalArgumentException(
                "Template version cannot be blank"
            );
        }

        if (version.length() > MAX_VERSION_LENGTH) {
            throw new IllegalArgumentException(
                "Template version exceeds maximum length"
            );
        }
    }
}
