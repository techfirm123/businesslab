package com.businesslab.industries.coffeeshop.template;

import com.businesslab.business.domain.BusinessConfiguration;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateCapability;
import com.businesslab.templates.domain.BusinessTemplateCategory;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import com.businesslab.templates.domain.BusinessTemplateValidationError;
import com.businesslab.templates.domain.BusinessTemplateValidationResult;
import com.businesslab.templates.domain.ConfigurationValueType;
import com.businesslab.templates.domain.TemplateConfigurationField;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Initial Coffee Shop business template.
 *
 * This first version establishes:
 *
 * - Template identity and version
 * - Business setup metadata
 * - Initial capital configuration
 * - Currency configuration
 * - Template-specific configuration validation
 *
 * Operational simulation mechanics will be introduced
 * in subsequent implementation milestones.
 */
@Component
public class CoffeeShopBusinessTemplate implements BusinessTemplate {

    public static final String TEMPLATE_ID = "coffee-shop";

    public static final String TEMPLATE_VERSION = "1.0.0";

    private static final String INITIAL_CAPITAL_FIELD = "initialCapital";

    private static final String CURRENCY_FIELD = "currency";

    private static final BigDecimal MINIMUM_INITIAL_CAPITAL =
        new BigDecimal("0.01");

    private static final String CURRENCY_PATTERN = "^[A-Z]{3}$";

    private static final Set<String> SUPPORTED_CONFIGURATION_FIELDS =
        Set.of(
            INITIAL_CAPITAL_FIELD,
            CURRENCY_FIELD
        );

    private static final BusinessTemplateDescriptor DESCRIPTOR =
        createDescriptor();

    @Override
    public BusinessTemplateDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public BusinessTemplateValidationResult validateConfiguration(
        BusinessConfiguration configuration
    ) {
        Objects.requireNonNull(
            configuration,
            "Business configuration cannot be null"
        );

        List<BusinessTemplateValidationError> errors =
            new ArrayList<>();

        validateInitialCapital(configuration, errors);

        validateCurrency(configuration, errors);

        validateUnknownFields(configuration, errors);

        if (errors.isEmpty()) {
            return BusinessTemplateValidationResult.valid();
        }

        return BusinessTemplateValidationResult.invalid(errors);
    }

    private void validateInitialCapital(
        BusinessConfiguration configuration,
        List<BusinessTemplateValidationError> errors
    ) {
        if (!configuration.values().containsKey(INITIAL_CAPITAL_FIELD)
            || configuration.get(INITIAL_CAPITAL_FIELD) == null) {
            errors.add(new BusinessTemplateValidationError(
                INITIAL_CAPITAL_FIELD,
                "REQUIRED",
                "Initial capital is required"
            ));

            return;
        }

        Object value = configuration.get(INITIAL_CAPITAL_FIELD);

        if (!(value instanceof Number number)) {
            errors.add(new BusinessTemplateValidationError(
                INITIAL_CAPITAL_FIELD,
                "INVALID_TYPE",
                "Initial capital must be a number"
            ));

            return;
        }

        BigDecimal amount = new BigDecimal(number.toString());

        if (amount.compareTo(MINIMUM_INITIAL_CAPITAL) < 0) {
            errors.add(new BusinessTemplateValidationError(
                INITIAL_CAPITAL_FIELD,
                "BELOW_MINIMUM",
                "Initial capital must be greater than zero"
            ));
        }
    }

    private void validateCurrency(
        BusinessConfiguration configuration,
        List<BusinessTemplateValidationError> errors
    ) {
        if (!configuration.values().containsKey(CURRENCY_FIELD)
            || configuration.get(CURRENCY_FIELD) == null) {
            errors.add(new BusinessTemplateValidationError(
                CURRENCY_FIELD,
                "REQUIRED",
                "Currency is required"
            ));

            return;
        }

        Object value = configuration.get(CURRENCY_FIELD);

        if (!(value instanceof String currency)) {
            errors.add(new BusinessTemplateValidationError(
                CURRENCY_FIELD,
                "INVALID_TYPE",
                "Currency must be a string"
            ));

            return;
        }

        if (!currency.matches(CURRENCY_PATTERN)) {
            errors.add(new BusinessTemplateValidationError(
                CURRENCY_FIELD,
                "INVALID_FORMAT",
                "Currency must be a three-letter uppercase currency code"
            ));
        }
    }

    private void validateUnknownFields(
        BusinessConfiguration configuration,
        List<BusinessTemplateValidationError> errors
    ) {
        configuration.values()
            .keySet()
            .stream()
            .filter(key ->
                !SUPPORTED_CONFIGURATION_FIELDS.contains(key)
            )
            .sorted(Comparator.naturalOrder())
            .forEach(key ->
                errors.add(new BusinessTemplateValidationError(
                    key,
                    "UNKNOWN_FIELD",
                    "Unsupported configuration field: " + key
                ))
            );
    }

    private static BusinessTemplateDescriptor createDescriptor() {
        return new BusinessTemplateDescriptor(
            new BusinessTemplateReference(
                TEMPLATE_ID,
                TEMPLATE_VERSION
            ),
            "Coffee Shop",
            "Start and configure a virtual coffee shop. "
                + "This template introduces the initial business "
                + "setup experience for BusinessLab.",
            BusinessTemplateCategory.FOOD_AND_BEVERAGE,
            BusinessTemplateStatus.ACTIVE,
            Set.of(
                BusinessTemplateCapability.BUSINESS_SETUP
            ),
            List.of(
                "coffee",
                "cafe",
                "food-and-beverage",
                "hospitality"
            ),
            List.of(
                new TemplateConfigurationField(
                    INITIAL_CAPITAL_FIELD,
                    "Initial Capital",
                    "The starting capital available to establish "
                        + "the business.",
                    ConfigurationValueType.DECIMAL,
                    true,
                    "100000",
                    MINIMUM_INITIAL_CAPITAL,
                    null,
                    null,
                    null
                ),
                new TemplateConfigurationField(
                    CURRENCY_FIELD,
                    "Currency",
                    "The currency used for the business "
                        + "financial configuration.",
                    ConfigurationValueType.STRING,
                    true,
                    "INR",
                    null,
                    null,
                    3,
                    CURRENCY_PATTERN
                )
            )
        );
    }
}
