package com.businesslab.templates.application;

import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BusinessTemplateRegistryIntegrationTest {

    private final BusinessTemplateRegistry registry;

    @Autowired
    BusinessTemplateRegistryIntegrationTest(
        BusinessTemplateRegistry registry
    ) {
        this.registry = registry;
    }

    @Test
    void shouldDiscoverCoffeeShopTemplateThroughSpring() {
        var template = registry.find(
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            )
        );

        assertThat(template).isPresent();

        assertThat(template.orElseThrow().descriptor().name())
            .isEqualTo("Coffee Shop");
    }

    @Test
    void shouldExposeCoffeeShopAsAvailableTemplate() {
        var availableTemplates = registry.findAvailable();

        assertThat(availableTemplates)
            .extracting(descriptor -> descriptor.reference().templateId())
            .contains("coffee-shop");
    }

    @Test
    void shouldExposeOnlyActiveTemplatesForNewBusinesses() {
        assertThat(registry.findAvailable())
            .allSatisfy(descriptor ->
                assertThat(descriptor.status())
                    .isEqualTo(BusinessTemplateStatus.ACTIVE)
            );
    }
}
