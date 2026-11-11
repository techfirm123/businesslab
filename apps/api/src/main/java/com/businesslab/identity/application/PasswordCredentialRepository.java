
package com.businesslab.identity.application;

import com.businesslab.identity.domain.UserId;

import java.time.Instant;
import java.util.Optional;

public interface PasswordCredentialRepository {

    void save(
        UserId userId,
        String encodedPassword,
        Instant createdAt
    );

    Optional<String> findEncodedPasswordByUserId(UserId userId);
}
