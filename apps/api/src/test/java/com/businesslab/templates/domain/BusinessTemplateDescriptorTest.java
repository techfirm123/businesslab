package com.businesslab.templates.domain;

import com.businesslab.business.domain.BusinessTemplateReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessTemplateDescriptorTest {

    private static final BusinessTemplateReference REFERENCE =
        new BusinessTemplateReference(
            "coffee-shop",
            "1.0.0"
        );

    @Test
    void shouldCreateValidTemplateDescriptor() {
        BusinessTemplateDescriptor descriptor = createDescriptor();

        assertThat(descriptor.name())
            .isEqualTo("Test Template");

        assertThat(descriptor.reference())
            .isEqualTo(REFERENCE);
    }

    @Test
    void shouldProtectCollectionsFromModification() {
        BusinessTemplateDescriptor descriptor = createDescriptor();

        assertThatThrownBy(
            () -> descriptor.tags().add("another-tag")
        )
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(
            () -> descriptor.configurationFields().add(
                createField("anotherField")
            )
        )
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(
            () -> descriptor.capabilities().add(
                BusinessTemplateCapability.BUSINESS_SETUP
            )
        )
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectBlankTemplateName() {
        assertThatThrownBy(
            () -> new BusinessTemplateDescriptor(
                REFERENCE,
                " ",
                "Test description",
                BusinessTemplateCategory.OTHER,
                BusinessTemplateStatus.ACTIVE,
                Set.of(BusinessTemplateCapability.BUSINESS_SETUP),
                List.of(),
                List.of()
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Template name cannot be blank");
    }

    @Test
    void shouldRejectDuplicateConfigurationFields() {
        assertThatThrownBy(
            () -> new BusinessTemplateDescriptor(
                REFERENCE,
                "Test Template",
                "Test description",
                BusinessTemplateCategory.OTHER,
                BusinessTemplateStatus.ACTIVE,
                Set.of(BusinessTemplateCapability.BUSINESS_SETUP),
                List.of(),
                List.of(
                    createField("initialCapital"),
                    createField("initialCapital")
                )
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Duplicate configuration field: initialCapital"
            );
    }

    private BusinessTemplateDescriptor createDescriptor() {
        return new BusinessTemplateDescriptor(
            REFERENCE,
            "Test Template",
            "Test template description",
            BusinessTemplateCategory.OTHER,
            BusinessTemplateStatus.ACTIVE,
            Set.of(BusinessTemplateCapability.BUSINESS_SETUP),
            List.of("test", "business"),
            List.of(createField("initialCapital"))
        );
    }

    private TemplateConfigurationField createField(String key) {
        return new TemplateConfigurationField(
            key,
            "Test Field",
            "Test configuration field",
            ConfigurationValueType.STRING,
            true,
            "example",
            null,
            null,
            null,
            null
        );
    }
}
