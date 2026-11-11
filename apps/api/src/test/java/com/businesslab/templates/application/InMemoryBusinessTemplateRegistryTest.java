package com.businesslab.templates.application;

import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.industries.coffeeshop.template.CoffeeShopBusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import com.businesslab.templates.infrastructure.registry.InMemoryBusinessTemplateRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryBusinessTemplateRegistryTest {

    private CoffeeShopBusinessTemplate coffeeShopTemplate;

    @BeforeEach
    void setUp() {
        coffeeShopTemplate = new CoffeeShopBusinessTemplate();
    }

    @Test
    void shouldResolveRegisteredTemplateByExactVersion() {
        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(coffeeShopTemplate)
            );

        var result = registry.find(
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            )
        );

        assertThat(result).isPresent();

        assertThat(result.orElseThrow())
            .isInstanceOf(CoffeeShopBusinessTemplate.class);
    }

    @Test
    void shouldNotResolveUnknownTemplate() {
        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(coffeeShopTemplate)
            );

        var result = registry.find(
            new BusinessTemplateReference(
                "unknown-business",
                "1.0.0"
            )
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotResolveDifferentTemplateVersion() {
        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(coffeeShopTemplate)
            );

        var result = registry.find(
            new BusinessTemplateReference(
                "coffee-shop",
                "2.0.0"
            )
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldResolveDeprecatedTemplateForExistingBusinesses() {
        BusinessTemplate deprecatedTemplate =
            createDeprecatedCoffeeShopTemplate();

        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(deprecatedTemplate)
            );

        var result = registry.find(
            new BusinessTemplateReference(
                "coffee-shop",
                "1.0.0"
            )
        );

        assertThat(result).isPresent();

        assertThat(result.orElseThrow().descriptor().status())
            .isEqualTo(BusinessTemplateStatus.DEPRECATED);
    }

    @Test
    void shouldExcludeDeprecatedTemplatesFromAvailableCatalogue() {
        BusinessTemplate deprecatedTemplate =
            createDeprecatedCoffeeShopTemplate();

        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(deprecatedTemplate)
            );

        assertThat(registry.findAvailable()).isEmpty();
    }

    @Test
    void shouldListAvailableTemplates() {
        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(
                List.of(coffeeShopTemplate)
            );

        var result = registry.findAvailable();

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().name())
            .isEqualTo("Coffee Shop");
    }

    @Test
    void shouldRejectDuplicateTemplateRegistrations() {
        List<BusinessTemplate> templates = List.of(
            coffeeShopTemplate,
            coffeeShopTemplate
        );

        assertThatThrownBy(
            () -> new InMemoryBusinessTemplateRegistry(templates)
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Duplicate business template registration: "
                    + "coffee-shop version 1.0.0"
            );
    }

    @Test
    void shouldReturnEmptyCatalogueWhenNoTemplatesAreRegistered() {
        BusinessTemplateRegistry registry =
            new InMemoryBusinessTemplateRegistry(List.of());

        assertThat(registry.findAvailable()).isEmpty();
    }

    private BusinessTemplate createDeprecatedCoffeeShopTemplate() {
        BusinessTemplateDescriptor original =
            coffeeShopTemplate.descriptor();

        BusinessTemplateDescriptor deprecatedDescriptor =
            new BusinessTemplateDescriptor(
                original.reference(),
                original.name(),
                original.description(),
                original.category(),
                BusinessTemplateStatus.DEPRECATED,
                original.capabilities(),
                original.tags(),
                original.configurationFields()
            );

        return new BusinessTemplate() {

            @Override
            public BusinessTemplateDescriptor descriptor() {
                return deprecatedDescriptor;
            }

            @Override
            public com.businesslab.templates.domain.BusinessTemplateValidationResult
            validateConfiguration(
                com.businesslab.business.domain.BusinessConfiguration configuration
            ) {
                return coffeeShopTemplate.validateConfiguration(configuration);
            }
        };
    }
}
