
package com.businesslab.identity.application;

import com.businesslab.identity.domain.EmailAddress;
import com.businesslab.identity.domain.User;
import com.businesslab.identity.domain.UserId;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
public class UserRegistrationService {

    private static final int MIN_PASSWORD_CODE_POINTS = 12;
    private static final int MAX_PASSWORD_UTF8_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserRegistrationService(
        UserRepository userRepository,
        PasswordCredentialRepository credentialRepository,
        PasswordEncoder passwordEncoder,
        Clock clock
    ) {
        this.userRepository = Objects.requireNonNull(
            userRepository,
            "User repository is required"
        );
        this.credentialRepository = Objects.requireNonNull(
            credentialRepository,
            "Password credential repository is required"
        );
        this.passwordEncoder = Objects.requireNonNull(
            passwordEncoder,
            "Password encoder is required"
        );
        this.clock = Objects.requireNonNull(
            clock,
            "Clock is required"
        );
    }

    /**
     * Registers a new user and stores the password as an adaptive hash.
     *
     * The account and its credential are persisted atomically.
     */
    @Transactional
    public User register(
        String rawEmail,
        String displayName,
        String rawPassword
    ) {
        EmailAddress email = EmailAddress.of(rawEmail);

        validatePassword(rawPassword);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant now = Instant.now(clock)
            .truncatedTo(ChronoUnit.MICROS);

        User user = User.create(
            UserId.generate(),
            email,
            displayName,
            now
        );

        String encodedPassword = passwordEncoder.encode(rawPassword);

        userRepository.save(user);

        credentialRepository.save(
            user.id(),
            encodedPassword,
            now
        );

        return user;
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                "Password is required"
            );
        }

        int codePointCount = rawPassword.codePointCount(
            0,
            rawPassword.length()
        );

        if (codePointCount < MIN_PASSWORD_CODE_POINTS) {
            throw new IllegalArgumentException(
                "Password must contain at least "
                    + MIN_PASSWORD_CODE_POINTS
                    + " characters"
            );
        }

        int utf8Length = rawPassword.getBytes(StandardCharsets.UTF_8).length;

        if (utf8Length > MAX_PASSWORD_UTF8_BYTES) {
            throw new IllegalArgumentException(
                "Password exceeds the maximum supported UTF-8 byte length"
            );
        }
    }
}
