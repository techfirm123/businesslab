package com.businesslab.business.domain;

import java.util.Objects;
import java.util.UUID;

public record BusinessId(UUID value) {

    public BusinessId {
        Objects.requireNonNull(value, "Business ID cannot be null");
    }

    public static BusinessId generate() {
        return new BusinessId(UUID.randomUUID());
    }

    public static BusinessId parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Business ID cannot be null or blank"
            );
        }

        return new BusinessId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
