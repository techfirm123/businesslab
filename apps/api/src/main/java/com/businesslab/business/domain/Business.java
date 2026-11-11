package com.businesslab.business.domain;

import com.businesslab.workspace.domain.WorkspaceId;

import java.time.Instant;
import java.util.Objects;

/**
 * Generic Business aggregate root.
 *
 * A Business represents an individual virtual business
 * within a workspace.
 *
 * Industry-specific behaviour is delegated to the
 * registered Business Template implementation.
 *
 * Business instances are immutable. Domain operations
 * return updated instances rather than modifying state
 * in place.
 */
public final class Business {

    private static final int MAX_NAME_LENGTH = 120;

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final BusinessId id;

    private final WorkspaceId workspaceId;

    private final BusinessTemplateReference templateReference;

    private final String name;

    private final String description;

    private final BusinessConfiguration configuration;

    private final BusinessStatus status;

    private final Instant createdAt;

    private final Instant updatedAt;

    private Business(
        BusinessId id,
        WorkspaceId workspaceId,
        BusinessTemplateReference templateReference,
        String name,
        String description,
        BusinessConfiguration configuration,
        BusinessStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(
            id,
            "Business ID is required"
        );

        this.workspaceId = Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );

        this.templateReference = Objects.requireNonNull(
            templateReference,
            "Business template reference is required"
        );

        this.name = validateName(name);
        this.description = normalizeDescription(description);

        this.configuration = Objects.requireNonNull(
            configuration,
            "Business configuration is required"
        );

        this.status = Objects.requireNonNull(
            status,
            "Business status is required"
        );

        this.createdAt = Objects.requireNonNull(
            createdAt,
            "Business creation timestamp is required"
        );

        this.updatedAt = Objects.requireNonNull(
            updatedAt,
            "Business update timestamp is required"
        );

        validateTimestampOrder(createdAt, updatedAt);
    }

    /**
     * Creates a new Business in DRAFT state.
     */
    public static Business create(
        BusinessId id,
        WorkspaceId workspaceId,
        BusinessTemplateReference templateReference,
        String name,
        String description,
        BusinessConfiguration configuration,
        Instant createdAt
    ) {
        return new Business(
            id,
            workspaceId,
            templateReference,
            name,
            description,
            configuration,
            BusinessStatus.DRAFT,
            createdAt,
            createdAt
        );
    }

    public Business activate(Instant updatedAt) {
        requireStatus(
            BusinessStatus.DRAFT,
            "Only draft businesses can be activated"
        );

        return copy(
            name,
            description,
            configuration,
            BusinessStatus.ACTIVE,
            updatedAt
        );
    }

    public Business pause(Instant updatedAt) {
        requireStatus(
            BusinessStatus.ACTIVE,
            "Only active businesses can be paused"
        );

        return copy(
            name,
            description,
            configuration,
            BusinessStatus.PAUSED,
            updatedAt
        );
    }

    public Business resume(Instant updatedAt) {
        requireStatus(
            BusinessStatus.PAUSED,
            "Only paused businesses can be resumed"
        );

        return copy(
            name,
            description,
            configuration,
            BusinessStatus.ACTIVE,
            updatedAt
        );
    }

    public Business close(Instant updatedAt) {
        if (status != BusinessStatus.ACTIVE
            && status != BusinessStatus.PAUSED) {
            throw new IllegalStateException(
                "Only active or paused businesses can be closed"
            );
        }

        return copy(
            name,
            description,
            configuration,
            BusinessStatus.CLOSED,
            updatedAt
        );
    }

    public Business archive(Instant updatedAt) {
        if (status == BusinessStatus.ACTIVE) {
            throw new IllegalStateException(
                "Active businesses must be paused or closed before archiving"
            );
        }

        if (status == BusinessStatus.ARCHIVED) {
            throw new IllegalStateException(
                "Business is already archived"
            );
        }

        return copy(
            name,
            description,
            configuration,
            BusinessStatus.ARCHIVED,
            updatedAt
        );
    }

    public Business rename(
        String newName,
        Instant updatedAt
    ) {
        ensureEditable();

        return copy(
            newName,
            description,
            configuration,
            status,
            updatedAt
        );
    }

    public Business updateDescription(
        String newDescription,
        Instant updatedAt
    ) {
        ensureEditable();

        return copy(
            name,
            newDescription,
            configuration,
            status,
            updatedAt
        );
    }

    public Business updateConfiguration(
        BusinessConfiguration newConfiguration,
        Instant updatedAt
    ) {
        ensureEditable();

        return copy(
            name,
            description,
            newConfiguration,
            status,
            updatedAt
        );
    }

    private Business copy(
        String name,
        String description,
        BusinessConfiguration configuration,
        BusinessStatus status,
        Instant updatedAt
    ) {
        return new Business(
            id,
            workspaceId,
            templateReference,
            name,
            description,
            configuration,
            status,
            createdAt,
            validateUpdateTimestamp(updatedAt)
        );
    }

    private void requireStatus(
        BusinessStatus expectedStatus,
        String message
    ) {
        if (status != expectedStatus) {
            throw new IllegalStateException(message);
        }
    }

    private void ensureEditable() {
        if (status == BusinessStatus.CLOSED
            || status == BusinessStatus.ARCHIVED) {
            throw new IllegalStateException(
                "Closed or archived businesses cannot be modified"
            );
        }
    }

    private Instant validateUpdateTimestamp(Instant timestamp) {
        Objects.requireNonNull(
            timestamp,
            "Update timestamp is required"
        );

        if (timestamp.isBefore(updatedAt)) {
            throw new IllegalArgumentException(
                "Update timestamp cannot be before the previous update"
            );
        }

        return timestamp;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Business name cannot be blank"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Business name cannot exceed "
                    + MAX_NAME_LENGTH
                    + " characters"
            );
        }

        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return "";
        }

        String normalized = description.trim();

        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                "Business description cannot exceed "
                    + MAX_DESCRIPTION_LENGTH
                    + " characters"
            );
        }

        return normalized;
    }

    private static void validateTimestampOrder(
        Instant createdAt,
        Instant updatedAt
    ) {
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                "Update timestamp cannot be before creation timestamp"
            );
        }
    }

    public BusinessId id() {
        return id;
    }

    public WorkspaceId workspaceId() {
        return workspaceId;
    }

    public BusinessTemplateReference templateReference() {
        return templateReference;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public BusinessConfiguration configuration() {
        return configuration;
    }

    public BusinessStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
