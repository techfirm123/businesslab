package com.businesslab.industries.coffeeshop.template;

import com.businesslab.business.domain.BusinessConfiguration;
import com.businesslab.templates.domain.BusinessTemplateCapability;
import com.businesslab.templates.domain.BusinessTemplateCategory;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoffeeShopBusinessTemplateTest {

    private final CoffeeShopBusinessTemplate template =
        new CoffeeShopBusinessTemplate();

    @Test
    void shouldExposeCorrectTemplateMetadata() {
        var descriptor = template.descriptor();

        assertThat(descriptor.reference().templateId())
            .isEqualTo("coffee-shop");

        assertThat(descriptor.reference().version())
            .isEqualTo("1.0.0");

        assertThat(descriptor.name())
            .isEqualTo("Coffee Shop");

        assertThat(descriptor.category())
            .isEqualTo(BusinessTemplateCategory.FOOD_AND_BEVERAGE);

        assertThat(descriptor.status())
            .isEqualTo(BusinessTemplateStatus.ACTIVE);

        assertThat(descriptor.capabilities())
            .containsExactly(
                BusinessTemplateCapability.BUSINESS_SETUP
            );
    }

    @Test
    void shouldExposeInitialCapitalConfigurationField() {
        var descriptor = template.descriptor();

        assertThat(descriptor.configurationFields())
            .extracting("key")
            .contains("initialCapital");
    }

    @Test
    void shouldExposeCurrencyConfigurationField() {
        var descriptor = template.descriptor();

        assertThat(descriptor.configurationFields())
            .extracting("key")
            .contains("currency");
    }

    @Test
    void shouldAcceptValidBusinessConfiguration() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital",
                    new BigDecimal("100000.00"),
                    "currency",
                    "INR"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isTrue();

        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldAcceptIntegerInitialCapital() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital",
                    100000,
                    "currency",
                    "INR"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldRejectMissingInitialCapital() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of("currency", "INR")
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isFalse();

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.fieldKey())
                    .isEqualTo("initialCapital");

                assertThat(error.code())
                    .isEqualTo("REQUIRED");
            });
    }

    @Test
    void shouldRejectMissingCurrency() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of("initialCapital", 100000)
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isFalse();

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.fieldKey())
                    .isEqualTo("currency");

                assertThat(error.code())
                    .isEqualTo("REQUIRED");
            });
    }

    @Test
    void shouldRejectNonNumericInitialCapital() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital",
                    "100000",
                    "currency",
                    "INR"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.fieldKey())
                    .isEqualTo("initialCapital");

                assertThat(error.code())
                    .isEqualTo("INVALID_TYPE");
            });
    }

    @Test
    void shouldRejectZeroInitialCapital() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital", 0,
                    "currency", "INR"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.code())
                    .isEqualTo("BELOW_MINIMUM");
            });
    }

    @Test
    void shouldRejectNegativeInitialCapital() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital", -1000,
                    "currency", "INR"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isFalse();

        assertThat(result.errors())
            .extracting("code")
            .contains("BELOW_MINIMUM");
    }

    @Test
    void shouldRejectInvalidCurrencyFormat() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital", 100000,
                    "currency", "inr"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.fieldKey())
                    .isEqualTo("currency");

                assertThat(error.code())
                    .isEqualTo("INVALID_FORMAT");
            });
    }

    @Test
    void shouldRejectUnsupportedConfigurationFields() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "initialCapital", 100000,
                    "currency", "INR",
                    "unknownField", "unexpected"
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.errors())
            .anySatisfy(error -> {
                assertThat(error.fieldKey())
                    .isEqualTo("unknownField");

                assertThat(error.code())
                    .isEqualTo("UNKNOWN_FIELD");
            });
    }

    @Test
    void shouldReturnMultipleValidationErrors() {
        BusinessConfiguration configuration =
            BusinessConfiguration.empty();

        var result = template.validateConfiguration(configuration);

        assertThat(result.isValid()).isFalse();

        assertThat(result.errors())
            .hasSize(2);
    }

    @Test
    void shouldExposeDeterministicValidationErrorOrder() {
        BusinessConfiguration configuration =
            BusinessConfiguration.of(
                Map.of(
                    "zUnknown", "value",
                    "aUnknown", "value",
                    "initialCapital", 100000
                )
            );

        var result = template.validateConfiguration(configuration);

        assertThat(result.errors())
            .extracting("fieldKey")
            .containsExactly(
                "currency",
                "aUnknown",
                "zUnknown"
            );
    }
}
