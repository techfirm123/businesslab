
package com.businesslab.identity.api.dto;

import com.businesslab.identity.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserRegistrationResponse(
    UUID userId,
    String email,
    String displayName,
    String status,
    Instant createdAt
) {

    public static UserRegistrationResponse from(User user) {
        return new UserRegistrationResponse(
            user.id().value(),
            user.email().value(),
            user.displayName(),
            user.status().name(),
            user.createdAt()
        );
    }
}
