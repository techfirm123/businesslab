
package com.businesslab.identity.api;

import com.businesslab.identity.api.dto.LoginRequest;
import com.businesslab.identity.api.dto.UserRegistrationResponse;
import com.businesslab.identity.application.CurrentUserService;
import com.businesslab.identity.application.InvalidCredentialsException;
import com.businesslab.identity.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final int MAX_PASSWORD_UTF8_BYTES = 72;

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final CurrentUserService currentUserService;

    public AuthenticationController(
        AuthenticationManager authenticationManager,
        SecurityContextRepository securityContextRepository,
        SessionAuthenticationStrategy sessionAuthenticationStrategy,
        CurrentUserService currentUserService
    ) {
        this.authenticationManager = Objects.requireNonNull(
            authenticationManager,
            "Authentication manager is required"
        );
        this.securityContextRepository = Objects.requireNonNull(
            securityContextRepository,
            "Security context repository is required"
        );
        this.sessionAuthenticationStrategy = Objects.requireNonNull(
            sessionAuthenticationStrategy,
            "Session authentication strategy is required"
        );
        this.currentUserService = Objects.requireNonNull(
            currentUserService,
            "Current user service is required"
        );
    }

    @PostMapping("/login")
    public UserRegistrationResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length
            > MAX_PASSWORD_UTF8_BYTES) {
            throw new InvalidCredentialsException();
        }

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    com.businesslab.identity.domain.EmailAddress
                        .of(request.email())
                        .value(),
                    request.password()
                )
            );
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            throw new InvalidCredentialsException();
        }

        /*
         * Rotate the pre-authentication session identifier and invalidate
         * the old CSRF token before establishing the authenticated context.
         */
        sessionAuthenticationStrategy.onAuthentication(
            authentication,
            servletRequest,
            servletResponse
        );

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        /*
         * Explicit persistence is required because this endpoint performs
         * authentication manually rather than through a security filter.
         */
        securityContextRepository.saveContext(
            context,
            servletRequest,
            servletResponse
        );

        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        return UserRegistrationResponse.from(user);
    }

    @GetMapping("/me")
    public UserRegistrationResponse currentUser(
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        return UserRegistrationResponse.from(user);
    }
}
