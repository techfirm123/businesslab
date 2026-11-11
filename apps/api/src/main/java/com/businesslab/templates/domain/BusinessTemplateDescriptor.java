package com.businesslab.templates.domain;

import com.businesslab.business.domain.BusinessTemplateReference;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable metadata describing a versioned business template.
 *
 * This object is intended for both internal registry operations
 * and future frontend template discovery APIs.
 */
public record BusinessTemplateDescriptor(
    BusinessTemplateReference reference,
    String name,
    String description,
    BusinessTemplateCategory category,
    BusinessTemplateStatus status,
    Set<BusinessTemplateCapability> capabilities,
    List<String> tags,
    List<TemplateConfigurationField> configurationFields
) {

    private static final int MAX_NAME_LENGTH = 120;

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public BusinessTemplateDescriptor {
        Objects.requireNonNull(
            reference,
            "Template reference is required"
        );

        Objects.requireNonNull(name, "Template name is required");
        Objects.requireNonNull(
            description,
            "Template description is required"
        );

        Objects.requireNonNull(category, "Template category is required");
        Objects.requireNonNull(status, "Template status is required");
        Objects.requireNonNull(capabilities, "Capabilities are required");
        Objects.requireNonNull(tags, "Tags are required");
        Objects.requireNonNull(
            configurationFields,
            "Configuration fields are required"
        );

        name = name.trim();
        description = description.trim();

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                "Template name cannot be blank"
            );
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Template name exceeds maximum length"
            );
        }

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                "Template description cannot be blank"
            );
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                "Template description exceeds maximum length"
            );
        }

        capabilities = Set.copyOf(capabilities);

        tags = tags.stream()
            .map(tag -> Objects.requireNonNull(
                tag,
                "Template tags cannot contain null values"
            ).trim())
            .toList();

        if (tags.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException(
                "Template tags cannot contain blank values"
            );
        }

        if (new HashSet<>(tags).size() != tags.size()) {
            throw new IllegalArgumentException(
                "Template tags cannot contain duplicates"
            );
        }

        configurationFields = List.copyOf(configurationFields);

        Set<String> fieldKeys = new HashSet<>();

        for (TemplateConfigurationField field : configurationFields) {
            Objects.requireNonNull(
                field,
                "Configuration fields cannot contain null values"
            );

            if (!fieldKeys.add(field.key())) {
                throw new IllegalArgumentException(
                    "Duplicate configuration field: " + field.key()
                );
            }
        }
    }
}
