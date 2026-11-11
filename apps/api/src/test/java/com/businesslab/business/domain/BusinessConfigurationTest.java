package com.businesslab.business.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessConfigurationTest {

    @Test
    void shouldCreateEmptyConfiguration() {
        BusinessConfiguration configuration =
            BusinessConfiguration.empty();

        assertThat(configuration.isEmpty()).isTrue();

        assertThat(configuration.values()).isEmpty();
    }

    @Test
    void shouldCreateConfigurationFromSimpleValues() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "name", "My Business",
                    "capital", 100000,
                    "active", true
                )
            );

        assertThat(configuration.get("name"))
            .isEqualTo("My Business");

        assertThat(configuration.get("capital"))
            .isEqualTo(100000);

        assertThat(configuration.get("active"))
            .isEqualTo(true);
    }

    @Test
    void shouldSupportNestedConfiguration() {
        Map<String, Object> operations = new LinkedHashMap<>();

        operations.put("openingHour", "09:00");
        operations.put("closingHour", "21:00");

        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "operations",
                    operations
                )
            );

        assertThat(configuration.values())
            .containsKey("operations");

        assertThat(configuration.values().get("operations"))
            .isInstanceOf(Map.class);
    }

    @Test
    void shouldDefensivelyCopySourceMap() {
        Map<String, Object> source = new LinkedHashMap<>();

        source.put("businessName", "Original");

        BusinessConfiguration configuration =
            BusinessConfiguration.of(source);

        source.put("businessName", "Modified");

        assertThat(configuration.get("businessName"))
            .isEqualTo("Original");
    }

    @Test
    void shouldProtectConfigurationMapFromModification() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of("name", "My Business")
            );

        assertThatThrownBy(
            () -> configuration.values().put(
                "name",
                "Modified"
            )
        )
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldProtectNestedCollectionsFromModification() {
        Map<String, Object> nested = new LinkedHashMap<>();

        nested.put("enabled", true);

        List<Object> items = new ArrayList<>();

        items.add("item-one");

        Map<String, Object> source = new LinkedHashMap<>();

        source.put("settings", nested);
        source.put("items", items);

        BusinessConfiguration configuration =
            BusinessConfiguration.of(source);

        Map<?, ?> frozenNested =
            (Map<?, ?>) configuration.get("settings");

        List<?> frozenItems =
            (List<?>) configuration.get("items");

        assertThatThrownBy(
            () -> attemptMapMutation(frozenNested)
        )
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(
            () -> attemptListMutation(frozenItems)
        )
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNonFiniteNumbers() {
        assertThatThrownBy(
            () -> BusinessConfiguration.of(
                Map.of("invalid", Double.NaN)
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Configuration numbers must be finite"
            );
    }

    @Test
    void shouldRejectUnsupportedValueTypes() {
        assertThatThrownBy(
            () -> BusinessConfiguration.of(
                Map.of("unsupported", new Object())
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(
                "Unsupported business configuration value type"
            );
    }

    @Test
    void shouldAllowNullValues() {
        Map<String, Object> source = new LinkedHashMap<>();

        source.put("optionalSetting", null);

        BusinessConfiguration configuration =
            BusinessConfiguration.of(source);

        assertThat(configuration.values())
            .containsKey("optionalSetting");

        assertThat(configuration.get("optionalSetting"))
            .isNull();
    }

    @Test
    void shouldRejectBlankConfigurationKeys() {
        assertThatThrownBy(
            () -> BusinessConfiguration.of(
                Map.of(" ", "invalid")
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Configuration keys cannot be null or blank"
            );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attemptMapMutation(Map<?, ?> map) {
        ((Map) map).put("enabled", false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attemptListMutation(List<?> list) {
        ((List) list).add("item-two");
    }
}
