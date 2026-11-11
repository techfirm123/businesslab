
package com.businesslab.workspace.api.dto;

import com.businesslab.workspace.application.WorkspaceMembershipDetails;
import com.businesslab.workspace.domain.Workspace;

import java.time.Instant;
import java.util.UUID;

/**
 * Workspace REST response with the requesting user's membership.
 */
public record WorkspaceResponse(
    UUID id,
    String name,
    String storageMode,
    String status,
    String membershipRole,
    String membershipStatus,
    Instant createdAt,
    Instant updatedAt
) {

    public static WorkspaceResponse from(
        WorkspaceMembershipDetails details
    ) {
        Workspace workspace = details.workspace();

        return new WorkspaceResponse(
            workspace.id().value(),
            workspace.name(),
            workspace.storageMode().name(),
            workspace.status().name(),
            details.membership().role().name(),
            details.membership().status().name(),
            workspace.createdAt(),
            workspace.updatedAt()
        );
    }
}
