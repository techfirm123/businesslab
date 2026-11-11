
package com.businesslab.platform.shared.api;

import com.businesslab.business.application.BusinessConfigurationValidationException;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        List<ApiErrorDetail> details = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toApiErrorDetail)
            .toList();

        return build(
            HttpStatus.BAD_REQUEST,
            "REQUEST_VALIDATION_FAILED",
            "Request validation failed",
            details,
            request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "Request body is missing or contains invalid JSON",
            List.of(),
            request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        String parameterName = exception.getName();

        return build(
            HttpStatus.BAD_REQUEST,
            "INVALID_PARAMETER",
            "Invalid value for parameter '" + parameterName + "'",
            List.of(new ApiErrorDetail(
                parameterName,
                "INVALID_FORMAT",
                "The supplied value has an invalid format"
            )),
            request
        );
    }

    @ExceptionHandler(BusinessConfigurationValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessConfiguration(
        BusinessConfigurationValidationException exception,
        HttpServletRequest request
    ) {
        List<ApiErrorDetail> details = exception.errors()
            .stream()
            .map(error -> new ApiErrorDetail(
                error.fieldKey(),
                error.code(),
                error.message()
            ))
            .toList();

        return build(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "BUSINESS_CONFIGURATION_INVALID",
            exception.getMessage(),
            details,
            request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            exception.getMessage(),
            List.of(),
            request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
        IllegalArgumentException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.BAD_REQUEST,
            "INVALID_ARGUMENT",
            exception.getMessage(),
            List.of(),
            request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
        IllegalStateException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.CONFLICT,
            "RESOURCE_STATE_CONFLICT",
            exception.getMessage(),
            List.of(),
            request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        return build(
            HttpStatus.CONFLICT,
            "DATA_CONFLICT",
            "The requested operation conflicts with an existing record or database constraint",
            List.of(),
            request
        );
    }

    private ApiErrorDetail toApiErrorDetail(FieldError error) {
        return new ApiErrorDetail(
            error.getField(),
            error.getCode() == null ? "INVALID_VALUE" : error.getCode(),
            error.getDefaultMessage() == null
                ? "Invalid value"
                : error.getDefaultMessage()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
        HttpStatus status,
        String code,
        String message,
        List<ApiErrorDetail> details,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            request.getRequestURI(),
            details
        );

        return ResponseEntity.status(status).body(response);
    }
}
