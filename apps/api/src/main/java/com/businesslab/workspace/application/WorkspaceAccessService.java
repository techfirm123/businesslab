
package com.businesslab.workspace.application;

import com.businesslab.identity.domain.UserId;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceMembership;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Centralizes workspace membership and permission checks.
 *
 * Inaccessible workspaces are reported as not found to avoid
 * disclosing the existence of another user's workspace.
 */
@Service
public class WorkspaceAccessService {

    private final WorkspaceMembershipRepository membershipRepository;

    public WorkspaceAccessService(
        WorkspaceMembershipRepository membershipRepository
    ) {
        this.membershipRepository = Objects.requireNonNull(
            membershipRepository,
            "Workspace membership repository is required"
        );
    }

    public WorkspaceMembership requireActiveMembership(
        WorkspaceId workspaceId,
        UserId userId
    ) {
        Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );
        Objects.requireNonNull(
            userId,
            "User ID is required"
        );

        return membershipRepository
            .findByWorkspaceIdAndUserId(workspaceId, userId)
            .filter(WorkspaceMembership::isActive)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Workspace",
                workspaceId.toString()
            ));
    }

    public WorkspaceMembership requireBusinessCreationAccess(
        WorkspaceId workspaceId,
        UserId userId
    ) {
        WorkspaceMembership membership =
            requireActiveMembership(workspaceId, userId);

        if (!membership.canCreateBusinesses()) {
            throw new WorkspacePermissionDeniedException(
                "Your workspace role does not permit business creation"
            );
        }

        return membership;
    }

    public WorkspaceMembership requireMemberManagementAccess(
        WorkspaceId workspaceId,
        UserId userId
    ) {
        WorkspaceMembership membership =
            requireActiveMembership(workspaceId, userId);

        if (!membership.canManageMembers()) {
            throw new WorkspacePermissionDeniedException(
                "Your workspace role does not permit membership management"
            );
        }

        return membership;
    }
}
