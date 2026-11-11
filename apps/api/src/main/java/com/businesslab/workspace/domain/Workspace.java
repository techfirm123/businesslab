package com.businesslab.workspace.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a user's BusinessLab workspace.
 *
 * A workspace is the organisational and persistence boundary
 * for one or more virtual businesses.
 *
 * Ownership and membership are deliberately excluded until
 * the platform identity foundation is implemented.
 */
public final class Workspace {

    private static final int MAX_NAME_LENGTH = 100;

    private final WorkspaceId id;

    private final String name;

    private final WorkspaceStorageMode storageMode;

    private final WorkspaceStatus status;

    private final Instant createdAt;

    private final Instant updatedAt;

    private Workspace(
        WorkspaceId id,
        String name,
        WorkspaceStorageMode storageMode,
        WorkspaceStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Workspace ID is required");
        this.name = validateName(name);

        this.storageMode = Objects.requireNonNull(
            storageMode,
            "Workspace storage mode is required"
        );

        this.status = Objects.requireNonNull(
            status,
            "Workspace status is required"
        );

        this.createdAt = Objects.requireNonNull(
            createdAt,
            "Creation timestamp is required"
        );

        this.updatedAt = Objects.requireNonNull(
            updatedAt,
            "Update timestamp is required"
        );

        validateTimestampOrder(createdAt, updatedAt);
    }

    public static Workspace create(
        WorkspaceId id,
        String name,
        WorkspaceStorageMode storageMode,
        Instant createdAt
    ) {
        return new Workspace(
            id,
            name,
            storageMode,
            WorkspaceStatus.ACTIVE,
            createdAt,
            createdAt
        );
    }

    public Workspace rename(String newName, Instant updatedAt) {
        ensureActive();

        return new Workspace(
            id,
            newName,
            storageMode,
            status,
            createdAt,
            validateUpdateTimestamp(updatedAt)
        );
    }

    /**
     * Promotes a local workspace to cloud persistence.
     *
     * This operation must only be invoked by an application
     * service after successful data migration and verification.
     */
    public Workspace promoteToCloud(Instant updatedAt) {
        ensureActive();

        if (storageMode == WorkspaceStorageMode.CLOUD) {
            throw new IllegalStateException(
                "Workspace already uses cloud storage"
            );
        }

        return new Workspace(
            id,
            name,
            WorkspaceStorageMode.CLOUD,
            status,
            createdAt,
            validateUpdateTimestamp(updatedAt)
        );
    }

    public Workspace archive(Instant updatedAt) {
        ensureActive();

        return new Workspace(
            id,
            name,
            storageMode,
            WorkspaceStatus.ARCHIVED,
            createdAt,
            validateUpdateTimestamp(updatedAt)
        );
    }

    private void ensureActive() {
        if (status != WorkspaceStatus.ACTIVE) {
            throw new IllegalStateException(
                "Archived workspaces cannot be modified"
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
                "Workspace name cannot be blank"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Workspace name cannot exceed "
                    + MAX_NAME_LENGTH
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

    public WorkspaceId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public WorkspaceStorageMode storageMode() {
        return storageMode;
    }

    public WorkspaceStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
