
package com.businesslab.workspace.api.dto;

import com.businesslab.workspace.domain.Workspace;

import java.time.Instant;
import java.util.UUID;

/**
 * Workspace REST response.
 */
public record WorkspaceResponse(
    UUID id,
    String name,
    String storageMode,
    String status,
    Instant createdAt,
    Instant updatedAt
) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
            workspace.id().value(),
            workspace.name(),
            workspace.storageMode().name(),
            workspace.status().name(),
            workspace.createdAt(),
            workspace.updatedAt()
        );
    }
}
