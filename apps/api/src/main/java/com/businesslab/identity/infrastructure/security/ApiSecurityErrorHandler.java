
package com.businesslab.identity.infrastructure.security;

import com.businesslab.platform.shared.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class ApiSecurityErrorHandler
    implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public ApiSecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        writeError(
            response,
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_REQUIRED",
            "Authentication is required",
            request
        );
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception
    ) throws IOException {

        if (exception instanceof CsrfException) {
            writeError(
                response,
                HttpStatus.FORBIDDEN,
                "CSRF_VALIDATION_FAILED",
                "CSRF token is missing or invalid",
                request
            );
            return;
        }

        writeError(
            response,
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "You do not have permission to perform this operation",
            request
        );
    }

    private void writeError(
        HttpServletResponse response,
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            request.getRequestURI(),
            List.of()
        );

        jsonMapper.writeValue(response.getOutputStream(), error);
    }
}
