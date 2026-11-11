
package com.businesslab.workspace.api;

import com.businesslab.business.application.BusinessApplicationService;
import com.businesslab.workspace.api.dto.WorkspaceCreateRequest;
import com.businesslab.workspace.api.dto.WorkspaceResponse;
import com.businesslab.workspace.application.WorkspaceBusinessQueryService;
import com.businesslab.workspace.domain.WorkspaceId;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Development-only workspace REST endpoints.
 *
 * These endpoints are disabled by default. They must not be exposed
 * in a production deployment before authentication and workspace
 * ownership authorization are implemented.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@Validated
@ConditionalOnProperty(
    prefix = "businesslab.api",
    name = "development-endpoints-enabled",
    havingValue = "true"
)
public class WorkspaceController {

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceBusinessQueryService queryService;

    public WorkspaceController(
        BusinessApplicationService businessApplicationService,
        WorkspaceBusinessQueryService queryService
    ) {
        this.businessApplicationService = businessApplicationService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
        @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        var workspace =
            businessApplicationService.createCloudWorkspace(request.name());

        WorkspaceResponse response = WorkspaceResponse.from(workspace);

        return ResponseEntity
            .created(URI.create("/api/v1/workspaces/" + response.id()))
            .body(response);
    }

    @GetMapping("/{workspaceId}")
    public WorkspaceResponse getWorkspace(
        @PathVariable UUID workspaceId
    ) {
        var workspace = queryService.getWorkspace(
            new WorkspaceId(workspaceId)
        );

        return WorkspaceResponse.from(workspace);
    }
}
