package com.businesslab.templates.infrastructure.registry;

import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.templates.application.BusinessTemplateRegistry;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable in-memory implementation of the business template registry.
 *
 * All template implementations are discovered through Spring
 * dependency injection.
 *
 * Duplicate template ID and version combinations are rejected
 * during application startup.
 */
@Component
public class InMemoryBusinessTemplateRegistry
    implements BusinessTemplateRegistry {

    private final Map<BusinessTemplateReference, BusinessTemplate> templates;

    public InMemoryBusinessTemplateRegistry(
        Collection<BusinessTemplate> registeredTemplates
    ) {
        Objects.requireNonNull(
            registeredTemplates,
            "Registered templates cannot be null"
        );

        Map<BusinessTemplateReference, BusinessTemplate> registry =
            new LinkedHashMap<>();

        for (BusinessTemplate template : registeredTemplates) {
            Objects.requireNonNull(
                template,
                "Registered template cannot be null"
            );

            BusinessTemplateDescriptor descriptor =
                Objects.requireNonNull(
                    template.descriptor(),
                    "Template descriptor cannot be null"
                );

            BusinessTemplateReference reference =
                descriptor.reference();

            BusinessTemplate existing =
                registry.putIfAbsent(reference, template);

            if (existing != null) {
                throw new IllegalStateException(
                    "Duplicate business template registration: "
                        + reference.templateId()
                        + " version "
                        + reference.version()
                );
            }
        }

        this.templates = Map.copyOf(registry);
    }

    @Override
    public Optional<BusinessTemplate> find(
        BusinessTemplateReference reference
    ) {
        Objects.requireNonNull(
            reference,
            "Template reference cannot be null"
        );

        return Optional.ofNullable(templates.get(reference));
    }

    @Override
    public List<BusinessTemplateDescriptor> findAvailable() {
        return templates.values()
            .stream()
            .map(BusinessTemplate::descriptor)
            .filter(descriptor ->
                descriptor.status() == BusinessTemplateStatus.ACTIVE
            )
            .sorted(
                Comparator
                    .comparing(
                        (BusinessTemplateDescriptor descriptor) ->
                            descriptor.category().name()
                    )
                    .thenComparing(
                        BusinessTemplateDescriptor::name,
                        String.CASE_INSENSITIVE_ORDER
                    )
                    .thenComparing(
                        descriptor ->
                            descriptor.reference().templateId()
                    )
            )
            .toList();
    }
}
