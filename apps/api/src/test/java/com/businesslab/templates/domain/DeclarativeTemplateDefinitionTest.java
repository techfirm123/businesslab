
package com.businesslab.templates.domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeclarativeTemplateDefinitionTest {

    @Test
    void shouldDefensivelyCopyNestedDefinitionData() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("displayName", "Bakery");

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("region", "India");

        metadata.put("market", nested);

        DeclarativeTemplateDefinition definition =
            new DeclarativeTemplateDefinition(
                1,
                metadata,
                List.of(),
                List.of(),
                List.of(),
                Map.of()
            );

        nested.put("region", "Changed");

        Map<?, ?> frozenMarket =
            (Map<?, ?>) definition.metadata().get("market");

        assertThat(frozenMarket.get("region"))
            .isEqualTo("India");

        assertThatThrownBy(
            () -> definition.metadata().put("anotherKey", "anotherValue")
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldAllowNullJsonValues() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("optionalValue", null);

        DeclarativeTemplateDefinition definition =
            new DeclarativeTemplateDefinition(
                1,
                metadata,
                List.of(),
                List.of(),
                List.of(),
                Map.of()
            );

        assertThat(definition.metadata())
            .containsKey("optionalValue");

        assertThat(definition.metadata().get("optionalValue"))
            .isNull();
    }

    @Test
    void shouldRejectUnsupportedExecutableObjectTypes() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("unsupported", new Object());

        assertThatThrownBy(
            () -> new DeclarativeTemplateDefinition(
                1,
                metadata,
                List.of(),
                List.of(),
                List.of(),
                Map.of()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(
                "Unsupported declarative definition value type"
            );
    }

    @Test
    void shouldRejectNonPositiveSchemaVersion() {
        assertThatThrownBy(
            () -> new DeclarativeTemplateDefinition(
                0,
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Definition schema version must be positive");
    }
}
