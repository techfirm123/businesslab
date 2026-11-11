
package com.businesslab.identity.infrastructure.security;

import com.businesslab.identity.application.PasswordCredentialRepository;
import com.businesslab.identity.application.UserRepository;
import com.businesslab.identity.domain.EmailAddress;
import com.businesslab.identity.domain.User;
import com.businesslab.identity.domain.UserStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BusinessLabUserDetailsService implements UserDetailsService {

    private static final String INVALID_CREDENTIALS =
        "Invalid email address or password";

    private final UserRepository userRepository;
    private final PasswordCredentialRepository credentialRepository;

    public BusinessLabUserDetailsService(
        UserRepository userRepository,
        PasswordCredentialRepository credentialRepository
    ) {
        this.userRepository = Objects.requireNonNull(
            userRepository,
            "User repository is required"
        );
        this.credentialRepository = Objects.requireNonNull(
            credentialRepository,
            "Credential repository is required"
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        final EmailAddress email;

        try {
            email = EmailAddress.of(username);
        } catch (IllegalArgumentException exception) {
            throw new UsernameNotFoundException(INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(INVALID_CREDENTIALS)
            );

        String encodedPassword = credentialRepository
            .findEncodedPasswordByUserId(user.id())
            .orElseThrow(() ->
                new UsernameNotFoundException(INVALID_CREDENTIALS)
            );

        return org.springframework.security.core.userdetails.User
            .withUsername(user.email().value())
            .password(encodedPassword)
            .authorities("ROLE_USER")
            .disabled(user.status() != UserStatus.ACTIVE)
            .build();
    }
}
