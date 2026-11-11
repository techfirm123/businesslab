
package com.businesslab.workspace.domain;

import com.businesslab.identity.domain.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the relationship between a user and a workspace.
 *
 * Memberships are immutable. Lifecycle and role changes return
 * a new instance rather than mutating the existing object.
 */
public final class WorkspaceMembership {

    private final WorkspaceMembershipId id;
    private final WorkspaceId workspaceId;
    private final UserId userId;
    private final WorkspaceMembershipRole role;
    private final WorkspaceMembershipStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private WorkspaceMembership(
        WorkspaceMembershipId id,
        WorkspaceId workspaceId,
        UserId userId,
        WorkspaceMembershipRole role,
        WorkspaceMembershipStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(
            id,
            "Membership ID is required"
        );
        this.workspaceId = Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );
        this.userId = Objects.requireNonNull(
            userId,
            "User ID is required"
        );
        this.role = Objects.requireNonNull(
            role,
            "Membership role is required"
        );
        this.status = Objects.requireNonNull(
            status,
            "Membership status is required"
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

    public static WorkspaceMembership createOwner(
        WorkspaceId workspaceId,
        UserId userId,
        Instant createdAt
    ) {
        return new WorkspaceMembership(
            WorkspaceMembershipId.generate(),
            workspaceId,
            userId,
            WorkspaceMembershipRole.OWNER,
            WorkspaceMembershipStatus.ACTIVE,
            createdAt,
            createdAt
        );
    }

    /**
     * Creates a non-owner membership.
     *
     * Ownership must be established through the dedicated
     * workspace creation or ownership-transfer workflows.
     */
    public static WorkspaceMembership createMember(
        WorkspaceId workspaceId,
        UserId userId,
        WorkspaceMembershipRole role,
        Instant createdAt
    ) {
        Objects.requireNonNull(role, "Membership role is required");

        if (role == WorkspaceMembershipRole.OWNER) {
            throw new IllegalArgumentException(
                "Owner membership must be created through createOwner"
            );
        }

        return new WorkspaceMembership(
            WorkspaceMembershipId.generate(),
            workspaceId,
            userId,
            role,
            WorkspaceMembershipStatus.ACTIVE,
            createdAt,
            createdAt
        );
    }

    /**
     * Reconstitutes a persisted membership.
     */
    public static WorkspaceMembership reconstitute(
        WorkspaceMembershipId id,
        WorkspaceId workspaceId,
        UserId userId,
        WorkspaceMembershipRole role,
        WorkspaceMembershipStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new WorkspaceMembership(
            id,
            workspaceId,
            userId,
            role,
            status,
            createdAt,
            updatedAt
        );
    }

    public WorkspaceMembership changeRole(
        WorkspaceMembershipRole newRole,
        Instant updatedAt
    ) {
        ensureActive();
        ensureNotOwner();

        Objects.requireNonNull(
            newRole,
            "New membership role is required"
        );

        if (newRole == WorkspaceMembershipRole.OWNER) {
            throw new IllegalArgumentException(
                "Ownership changes require a dedicated transfer operation"
            );
        }

        if (newRole == role) {
            return this;
        }

        return copy(
            newRole,
            status,
            validateUpdateTimestamp(updatedAt)
        );
    }

    public WorkspaceMembership suspend(Instant updatedAt) {
        ensureActive();
        ensureNotOwner();

        return copy(
            role,
            WorkspaceMembershipStatus.SUSPENDED,
            validateUpdateTimestamp(updatedAt)
        );
    }

    public WorkspaceMembership reactivate(Instant updatedAt) {
        if (status == WorkspaceMembershipStatus.ACTIVE) {
            throw new IllegalStateException(
                "Membership is already active"
            );
        }

        if (status != WorkspaceMembershipStatus.SUSPENDED
            && status != WorkspaceMembershipStatus.REMOVED) {
            throw new IllegalStateException(
                "Membership cannot be reactivated"
            );
        }

        return copy(
            role,
            WorkspaceMembershipStatus.ACTIVE,
            validateUpdateTimestamp(updatedAt)
        );
    }

    public WorkspaceMembership remove(Instant updatedAt) {
        ensureNotOwner();

        if (status == WorkspaceMembershipStatus.REMOVED) {
            throw new IllegalStateException(
                "Membership is already removed"
            );
        }

        return copy(
            role,
            WorkspaceMembershipStatus.REMOVED,
            validateUpdateTimestamp(updatedAt)
        );
    }

    public boolean isActive() {
        return status == WorkspaceMembershipStatus.ACTIVE;
    }

    public boolean canReadWorkspace() {
        return isActive() && role.canReadWorkspace();
    }

    public boolean canCreateBusinesses() {
        return isActive() && role.canCreateBusinesses();
    }

    public boolean canManageMembers() {
        return isActive() && role.canManageMembers();
    }

    private WorkspaceMembership copy(
        WorkspaceMembershipRole newRole,
        WorkspaceMembershipStatus newStatus,
        Instant updatedAt
    ) {
        return new WorkspaceMembership(
            id,
            workspaceId,
            userId,
            newRole,
            newStatus,
            createdAt,
            updatedAt
        );
    }

    private void ensureActive() {
        if (status != WorkspaceMembershipStatus.ACTIVE) {
            throw new IllegalStateException(
                "Only active memberships can perform this operation"
            );
        }
    }

    private void ensureNotOwner() {
        if (role == WorkspaceMembershipRole.OWNER) {
            throw new IllegalStateException(
                "Owner membership cannot be modified through member lifecycle operations"
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

    public WorkspaceMembershipId id() {
        return id;
    }

    public WorkspaceId workspaceId() {
        return workspaceId;
    }

    public UserId userId() {
        return userId;
    }

    public WorkspaceMembershipRole role() {
        return role;
    }

    public WorkspaceMembershipStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
