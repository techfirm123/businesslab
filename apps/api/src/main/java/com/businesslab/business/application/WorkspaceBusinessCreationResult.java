
package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.workspace.domain.Workspace;

import java.util.Objects;

/**
 * Result of atomically creating a cloud workspace and its first business.
 */
public record WorkspaceBusinessCreationResult(
    Workspace workspace,
    Business business
) {

    public WorkspaceBusinessCreationResult {
        Objects.requireNonNull(
            workspace,
            "Workspace is required"
        );

        Objects.requireNonNull(
            business,
            "Business is required"
        );

        if (!business.workspaceId().equals(workspace.id())) {
            throw new IllegalArgumentException(
                "Business must belong to the returned workspace"
            );
        }
    }
}
