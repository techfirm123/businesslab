
package com.businesslab.workspace.application;

import com.businesslab.identity.domain.UserId;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceMembership;
import com.businesslab.workspace.domain.WorkspaceStorageMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates workspace creation and membership-scoped access.
 */
@Service
public class WorkspaceApplicationService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository membershipRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final Clock clock;

    public WorkspaceApplicationService(
        WorkspaceRepository workspaceRepository,
        WorkspaceMembershipRepository membershipRepository,
        WorkspaceAccessService workspaceAccessService,
        Clock clock
    ) {
        this.workspaceRepository = Objects.requireNonNull(
            workspaceRepository,
            "Workspace repository is required"
        );
        this.membershipRepository = Objects.requireNonNull(
            membershipRepository,
            "Workspace membership repository is required"
        );
        this.workspaceAccessService = Objects.requireNonNull(
            workspaceAccessService,
            "Workspace access service is required"
        );
        this.clock = Objects.requireNonNull(
            clock,
            "Clock is required"
        );
    }

    /**
     * Creates a cloud workspace and its initial owner membership atomically.
     */
    @Transactional
    public WorkspaceMembershipDetails createCloudWorkspace(
        String name,
        UserId ownerUserId
    ) {
        Objects.requireNonNull(
            ownerUserId,
            "Workspace owner user ID is required"
        );

        Instant now = currentInstant();

        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            name,
            WorkspaceStorageMode.CLOUD,
            now
        );

        WorkspaceMembership ownerMembership =
            WorkspaceMembership.createOwner(
                workspace.id(),
                ownerUserId,
                now
            );

        workspaceRepository.save(workspace);
        membershipRepository.save(ownerMembership);

        return new WorkspaceMembershipDetails(
            workspace,
            ownerMembership
        );
    }

    /**
     * Returns all active workspace memberships for the supplied user.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceMembershipDetails> getWorkspaces(
        UserId userId
    ) {
        Objects.requireNonNull(userId, "User ID is required");

        List<WorkspaceMembership> memberships =
            membershipRepository.findActiveByUserId(userId);

        Map<WorkspaceId, WorkspaceMembership> membershipByWorkspace =
            new HashMap<>();

        for (WorkspaceMembership membership : memberships) {
            membershipByWorkspace.put(
                membership.workspaceId(),
                membership
            );
        }

        List<Workspace> workspaces =
            workspaceRepository.findAllForUser(userId);

        return workspaces.stream()
            .map(workspace -> {
                WorkspaceMembership membership =
                    membershipByWorkspace.get(workspace.id());

                if (membership == null) {
                    throw new IllegalStateException(
                        "Workspace membership query returned inconsistent data"
                    );
                }

                return new WorkspaceMembershipDetails(
                    workspace,
                    membership
                );
            })
            .toList();
    }

    /**
     * Retrieves a workspace only when the requesting user
     * has an active membership.
     */
    @Transactional(readOnly = true)
    public WorkspaceMembershipDetails getWorkspace(
        WorkspaceId workspaceId,
        UserId userId
    ) {
        WorkspaceMembership membership =
            workspaceAccessService.requireActiveMembership(
                workspaceId,
                userId
            );

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Workspace",
                workspaceId.toString()
            ));

        return new WorkspaceMembershipDetails(
            workspace,
            membership
        );
    }

    private Instant currentInstant() {
        // PostgreSQL TIMESTAMPTZ is stored at microsecond precision.
        return Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
    }
}
