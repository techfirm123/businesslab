
package com.businesslab.identity.domain;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "User ID is required");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
