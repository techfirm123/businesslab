package com.businesslab.templates.domain;

import com.businesslab.business.domain.BusinessTemplateReference;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persisted identity, visibility and governance metadata for one exact
 * declarative template version.
 */
public record DeclarativeBusinessTemplateVersion(
    BusinessTemplateReference reference,
    String name,
    String description,
    BusinessTemplateCategory category,
    DeclarativeTemplateLifecycleStatus lifecycleStatus,
    TemplateVisibility visibility,
    UUID ownerWorkspaceId,
    DeclarativeTemplateDefinition definition,
    String provenance,
    UUID createdBy,
    UUID approvedBy,
    Instant approvedAt,
    Instant publishedAt,
    Instant createdAt
) {

    public DeclarativeBusinessTemplateVersion {
        Objects.requireNonNull(reference, "Template reference is required");
        Objects.requireNonNull(name, "Template name is required");
        Objects.requireNonNull(description, "Description is required");
        Objects.requireNonNull(category, "Category is required");
        Objects.requireNonNull(lifecycleStatus, "Lifecycle status is required");
        Objects.requireNonNull(visibility, "Visibility is required");
        Objects.requireNonNull(definition, "Declarative definition is required");
        Objects.requireNonNull(provenance, "Provenance is required");
        Objects.requireNonNull(createdAt, "Creation timestamp is required");

        name = name.trim();
        description = description.trim();
        provenance = provenance.trim();

        if (name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("Template name must contain 1 to 120 characters");
        }
        if (description.isBlank() || description.length() > 1000) {
            throw new IllegalArgumentException("Template description must contain 1 to 1000 characters");
        }
        if (provenance.length() > 10000) {
            throw new IllegalArgumentException("Template provenance is too large");
        }

        if (visibility == TemplateVisibility.PLATFORM && ownerWorkspaceId != null) {
            throw new IllegalArgumentException("Platform templates cannot have a workspace owner");
        }
        if (visibility != TemplateVisibility.PLATFORM && ownerWorkspaceId == null) {
            throw new IllegalArgumentException("Non-platform templates must have an owning workspace");
        }
        if ((approvedAt == null) != (approvedBy == null)) {
            throw new IllegalArgumentException("Approval identity and timestamp must be supplied together");
        }
        if ((lifecycleStatus == DeclarativeTemplateLifecycleStatus.APPROVED
            || lifecycleStatus == DeclarativeTemplateLifecycleStatus.PUBLISHED
            || lifecycleStatus == DeclarativeTemplateLifecycleStatus.DEPRECATED)
            && approvedAt == null) {
            throw new IllegalArgumentException("Approved, published and deprecated versions require approval metadata");
        }
        if ((lifecycleStatus == DeclarativeTemplateLifecycleStatus.PUBLISHED
            || lifecycleStatus == DeclarativeTemplateLifecycleStatus.DEPRECATED)
            && publishedAt == null) {
            throw new IllegalArgumentException("Published and deprecated versions require publication metadata");
        }
    }

    public boolean isDiscoverable() {
        return lifecycleStatus == DeclarativeTemplateLifecycleStatus.PUBLISHED
            && (visibility == TemplateVisibility.PLATFORM || visibility == TemplateVisibility.PUBLIC);
    }
}
