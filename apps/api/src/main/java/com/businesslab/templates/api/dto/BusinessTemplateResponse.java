
package com.businesslab.templates.api.dto;

import com.businesslab.templates.domain.BusinessTemplateDescriptor;

import java.util.List;

public record BusinessTemplateResponse(
    String templateId,
    String version,
    String sourceType,
    String name,
    String description,
    String category,
    String status,
    List<String> capabilities,
    List<String> tags,
    List<TemplateConfigurationFieldResponse> configurationFields
) {

    public static BusinessTemplateResponse from(
        BusinessTemplateDescriptor descriptor
    ) {
        List<String> capabilities = descriptor.capabilities()
            .stream()
            .map(Enum::name)
            .sorted()
            .toList();

        List<TemplateConfigurationFieldResponse> fields =
            descriptor.configurationFields()
                .stream()
                .map(TemplateConfigurationFieldResponse::from)
                .toList();

        return new BusinessTemplateResponse(
            descriptor.reference().templateId(),
            descriptor.reference().version(),
            "CODE_BACKED",
            descriptor.name(),
            descriptor.description(),
            descriptor.category().name(),
            descriptor.status().name(),
            capabilities,
            descriptor.tags(),
            fields
        );
    }
}
