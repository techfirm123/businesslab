
package com.businesslab.business.api.dto;

import com.businesslab.business.domain.Business;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Business REST response.
 */
public record BusinessResponse(
    UUID id,
    UUID workspaceId,
    String templateId,
    String templateVersion,
    String name,
    String description,
    String status,
    Map<String, Object> configuration,
    Instant createdAt,
    Instant updatedAt
) {

    public static BusinessResponse from(Business business) {
        return new BusinessResponse(
            business.id().value(),
            business.workspaceId().value(),
            business.templateReference().templateId(),
            business.templateReference().version(),
            business.name(),
            business.description(),
            business.status().name(),
            business.configuration().values(),
            business.createdAt(),
            business.updatedAt()
        );
    }
}
