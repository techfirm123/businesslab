
package com.businesslab.workspace.application;

import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceMembership;

import java.util.Objects;

/**
 * Workspace details together with the requesting user's membership.
 */
public record WorkspaceMembershipDetails(
    Workspace workspace,
    WorkspaceMembership membership
) {

    public WorkspaceMembershipDetails {
        Objects.requireNonNull(
            workspace,
            "Workspace is required"
        );
        Objects.requireNonNull(
            membership,
            "Workspace membership is required"
        );

        if (!workspace.id().equals(membership.workspaceId())) {
            throw new IllegalArgumentException(
                "Workspace and membership identifiers do not match"
            );
        }
    }
}
