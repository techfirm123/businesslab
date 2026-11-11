package com.businesslab.business.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, industry-neutral business configuration.
 *
 * Supported values follow JSON-compatible data types:
 *
 * - String
 * - Boolean
 * - Null
 * - Integer and decimal numbers
 * - Nested maps with String keys
 * - Lists
 *
 * Template-specific validation belongs to the
 * Business Template implementation.
 */
public final class BusinessConfiguration {

    private static final BusinessConfiguration EMPTY =
        new BusinessConfiguration(Map.of());

    private final Map<String, Object> values;

    private BusinessConfiguration(Map<String, Object> values) {
        this.values = freezeMap(values);
    }

    public static BusinessConfiguration empty() {
        return EMPTY;
    }

    public static BusinessConfiguration of(
        Map<String, ?> values
    ) {
        Objects.requireNonNull(
            values,
            "Business configuration cannot be null"
        );

        if (values.isEmpty()) {
            return EMPTY;
        }

        return new BusinessConfiguration(
            copyMap(values)
        );
    }

    public Map<String, Object> values() {
        return values;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Object get(String key) {
        return values.get(key);
    }

    private static Map<String, Object> copyMap(
        Map<String, ?> source
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        source.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(
                    "Configuration keys cannot be null or blank"
                );
            }

            result.put(key, freezeValue(value));
        });

        return result;
    }

    private static Map<String, Object> freezeMap(
        Map<String, Object> source
    ) {
        return Collections.unmodifiableMap(
            new LinkedHashMap<>(source)
        );
    }

    private static Object freezeValue(Object value) {
        if (value == null
            || value instanceof String
            || value instanceof Boolean
            || value instanceof Integer
            || value instanceof Long
            || value instanceof Short
            || value instanceof Byte
            || value instanceof BigDecimal
            || value instanceof BigInteger) {

            return value;
        }

        if (value instanceof Double number) {
            if (!Double.isFinite(number)) {
                throw new IllegalArgumentException(
                    "Configuration numbers must be finite"
                );
            }

            return number;
        }

        if (value instanceof Float number) {
            if (!Float.isFinite(number)) {
                throw new IllegalArgumentException(
                    "Configuration numbers must be finite"
                );
            }

            return number;
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();

            map.forEach((key, nestedValue) -> {
                if (!(key instanceof String stringKey)
                    || stringKey.isBlank()) {
                    throw new IllegalArgumentException(
                        "Nested configuration keys must be non-blank strings"
                    );
                }

                nested.put(
                    stringKey,
                    freezeValue(nestedValue)
                );
            });

            return Collections.unmodifiableMap(nested);
        }

        if (value instanceof List<?> list) {
            List<Object> nested = new ArrayList<>(list.size());

            for (Object item : list) {
                nested.add(freezeValue(item));
            }

            return Collections.unmodifiableList(nested);
        }

        throw new IllegalArgumentException(
            "Unsupported business configuration value type: "
                + value.getClass().getName()
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof BusinessConfiguration that)) {
            return false;
        }

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "BusinessConfiguration" + values;
    }
}
