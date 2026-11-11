
package com.businesslab.workspace.api;

import com.businesslab.identity.application.CurrentUserService;
import com.businesslab.identity.domain.User;
import com.businesslab.workspace.api.dto.WorkspaceCreateRequest;
import com.businesslab.workspace.api.dto.WorkspaceResponse;
import com.businesslab.workspace.application.WorkspaceApplicationService;
import com.businesslab.workspace.domain.WorkspaceId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Authenticated workspace REST endpoints.
 *
 * Workspace access is always scoped to the authenticated user's
 * active membership.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@Validated
public class WorkspaceController {

    private final WorkspaceApplicationService workspaceApplicationService;
    private final CurrentUserService currentUserService;

    public WorkspaceController(
        WorkspaceApplicationService workspaceApplicationService,
        CurrentUserService currentUserService
    ) {
        this.workspaceApplicationService = workspaceApplicationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
        @Valid @RequestBody WorkspaceCreateRequest request,
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        var details =
            workspaceApplicationService.createCloudWorkspace(
                request.name(),
                user.id()
            );

        WorkspaceResponse response =
            WorkspaceResponse.from(details);

        return ResponseEntity
            .created(URI.create("/api/v1/workspaces/" + response.id()))
            .body(response);
    }

    @GetMapping
    public List<WorkspaceResponse> getWorkspaces(
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        return workspaceApplicationService.getWorkspaces(user.id())
            .stream()
            .map(WorkspaceResponse::from)
            .toList();
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getWorkspace(
        @PathVariable UUID workspaceId,
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        var details = workspaceApplicationService.getWorkspace(
            new WorkspaceId(workspaceId),
            user.id()
        );

        return WorkspaceResponse.from(details);
    }
}
