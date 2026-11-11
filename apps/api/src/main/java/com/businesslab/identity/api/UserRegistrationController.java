
package com.businesslab.identity.api;

import com.businesslab.identity.api.dto.UserRegistrationRequest;
import com.businesslab.identity.api.dto.UserRegistrationResponse;
import com.businesslab.identity.application.UserRegistrationService;
import com.businesslab.identity.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    public UserRegistrationController(
        UserRegistrationService userRegistrationService
    ) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse register(
        @Valid @RequestBody UserRegistrationRequest request
    ) {
        User user = userRegistrationService.register(
            request.email(),
            request.displayName(),
            request.password()
        );

        return UserRegistrationResponse.from(user);
    }
}
