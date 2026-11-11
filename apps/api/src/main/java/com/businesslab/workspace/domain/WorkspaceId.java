package com.businesslab.workspace.domain;

import java.util.Objects;
import java.util.UUID;

public record WorkspaceId(UUID value) {

    public WorkspaceId {
        Objects.requireNonNull(value, "Workspace ID cannot be null");
    }

    public static WorkspaceId generate() {
        return new WorkspaceId(UUID.randomUUID());
    }

    public static WorkspaceId parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Workspace ID cannot be null or blank"
            );
        }

        return new WorkspaceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
