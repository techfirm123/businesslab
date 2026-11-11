
package com.businesslab.templates.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Safe, JSON-compatible declarative template payload.
 *
 * The definition is data, never executable source. The platform-owned
 * schema validator and approved capability catalogue remain authoritative.
 */
public record DeclarativeTemplateDefinition(
    int schemaVersion,
    Map<String, Object> metadata,
    List<Map<String, Object>> capabilities,
    List<Map<String, Object>> workflows,
    List<Map<String, Object>> scenarios,
    Map<String, Object> assumptions
) {

    public DeclarativeTemplateDefinition {
        if (schemaVersion < 1) {
            throw new IllegalArgumentException(
                "Definition schema version must be positive"
            );
        }

        metadata = immutableMap(
            Objects.requireNonNull(metadata, "Metadata is required")
        );

        capabilities = immutableListOfMaps(
            Objects.requireNonNull(capabilities, "Capabilities are required")
        );

        workflows = immutableListOfMaps(
            Objects.requireNonNull(workflows, "Workflows are required")
        );

        scenarios = immutableListOfMaps(
            Objects.requireNonNull(scenarios, "Scenarios are required")
        );

        assumptions = immutableMap(
            Objects.requireNonNull(assumptions, "Assumptions are required")
        );
    }

    private static Map<String, Object> immutableMap(
        Map<String, Object> source
    ) {
        Map<String, Object> copy = new LinkedHashMap<>();

        source.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(
                    "Definition object keys must be non-blank"
                );
            }

            copy.put(key, freeze(value));
        });

        return Collections.unmodifiableMap(copy);
    }

    private static List<Map<String, Object>> immutableListOfMaps(
        List<Map<String, Object>> source
    ) {
        List<Map<String, Object>> copy = new ArrayList<>(source.size());

        for (Map<String, Object> item : source) {
            copy.add(immutableMap(
                Objects.requireNonNull(
                    item,
                    "Definition arrays cannot contain null objects"
                )
            ));
        }

        return List.copyOf(copy);
    }

    private static Object freeze(Object value) {
        if (value == null
            || value instanceof String
            || value instanceof Boolean
            || value instanceof Integer
            || value instanceof Long
            || value instanceof Short
            || value instanceof Byte
            || value instanceof BigInteger
            || value instanceof BigDecimal) {
            return value;
        }

        if (value instanceof Double number) {
            if (!Double.isFinite(number)) {
                throw new IllegalArgumentException(
                    "Definition numbers must be finite"
                );
            }

            return number;
        }

        if (value instanceof Float number) {
            if (!Float.isFinite(number)) {
                throw new IllegalArgumentException(
                    "Definition numbers must be finite"
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
                        "Definition object keys must be non-blank strings"
                    );
                }

                nested.put(stringKey, freeze(nestedValue));
            });

            return Collections.unmodifiableMap(nested);
        }

        if (value instanceof List<?> list) {
            List<Object> nested = new ArrayList<>(list.size());

            for (Object item : list) {
                nested.add(freeze(item));
            }

            return Collections.unmodifiableList(nested);
        }

        throw new IllegalArgumentException(
            "Unsupported declarative definition value type: "
                + value.getClass().getName()
        );
    }
}
