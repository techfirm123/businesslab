
package com.businesslab.identity.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern BASIC_EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public EmailAddress {
        Objects.requireNonNull(value, "Email address is required");

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank() || normalized.length() > 320) {
            throw new IllegalArgumentException(
                "Email address must contain between 1 and 320 characters"
            );
        }

        if (!BASIC_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Email address has an invalid format"
            );
        }

        value = normalized;
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
