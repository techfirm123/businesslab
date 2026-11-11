
package com.businesslab.identity.application;

import com.businesslab.identity.domain.EmailAddress;
import com.businesslab.identity.domain.User;
import com.businesslab.identity.domain.UserStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Resolves the currently authenticated identity to an active user.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(
            userRepository,
            "User repository is required"
        );
    }

    public User requireActiveUser(String authenticatedEmail) {
        EmailAddress email = EmailAddress.of(authenticatedEmail);

        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);

        if (user.status() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
