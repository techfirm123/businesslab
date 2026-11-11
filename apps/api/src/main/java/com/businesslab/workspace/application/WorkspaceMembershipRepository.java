
package com.businesslab.workspace.application;

import com.businesslab.identity.domain.UserId;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceMembership;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for workspace memberships.
 */
public interface WorkspaceMembershipRepository {

    void save(WorkspaceMembership membership);

    void update(WorkspaceMembership membership);

    Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(
        WorkspaceId workspaceId,
        UserId userId
    );

    List<WorkspaceMembership> findActiveByUserId(UserId userId);
}
