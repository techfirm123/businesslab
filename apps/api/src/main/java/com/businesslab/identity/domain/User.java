
package com.businesslab.identity.domain;

import java.time.Instant;
import java.util.Objects;

public record User(
    UserId id,
    EmailAddress email,
    String displayName,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt
) {

    public User {
        Objects.requireNonNull(id, "User ID is required");
        Objects.requireNonNull(email, "Email address is required");
        Objects.requireNonNull(status, "User status is required");
        Objects.requireNonNull(createdAt, "Creation time is required");
        Objects.requireNonNull(updatedAt, "Update time is required");

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                "Display name is required"
            );
        }

        String normalizedDisplayName = displayName.trim();

        if (normalizedDisplayName.length() > 100) {
            throw new IllegalArgumentException(
                "Display name cannot exceed 100 characters"
            );
        }

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                "Update time cannot be earlier than creation time"
            );
        }

        displayName = normalizedDisplayName;
    }

    public static User create(
        UserId id,
        EmailAddress email,
        String displayName,
        Instant now
    ) {
        return new User(
            id,
            email,
            displayName,
            UserStatus.ACTIVE,
            now,
            now
        );
    }

    public static User reconstitute(
        UserId id,
        EmailAddress email,
        String displayName,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new User(
            id,
            email,
            displayName,
            status,
            createdAt,
            updatedAt
        );
    }
}
