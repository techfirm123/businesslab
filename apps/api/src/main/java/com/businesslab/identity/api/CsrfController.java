
package com.businesslab.identity.api;

import com.businesslab.identity.api.dto.CsrfTokenResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class CsrfController {

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
            csrfToken.getToken(),
            csrfToken.getHeaderName(),
            csrfToken.getParameterName()
        );
    }
}
