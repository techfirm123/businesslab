
package com.businesslab.workspace.domain;

import java.util.Objects;
import java.util.UUID;

public record WorkspaceMembershipId(UUID value) {

    public WorkspaceMembershipId {
        Objects.requireNonNull(
            value,
            "Workspace membership ID is required"
        );
    }

    public static WorkspaceMembershipId generate() {
        return new WorkspaceMembershipId(UUID.randomUUID());
    }

    public static WorkspaceMembershipId parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Workspace membership ID cannot be blank"
            );
        }

        return new WorkspaceMembershipId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
