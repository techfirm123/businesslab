package com.businesslab.business.api;

import com.businesslab.business.api.dto.BusinessCreateRequest;
import com.businesslab.business.api.dto.BusinessResponse;
import com.businesslab.business.application.BusinessApplicationService;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.identity.application.CurrentUserService;
import com.businesslab.identity.domain.User;
import com.businesslab.workspace.application.WorkspaceBusinessQueryService;
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
 * Authenticated business REST endpoints.
 *
 * Workspace membership and role authorization are enforced by
 * the application services, not by this controller.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
public class BusinessController {

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceBusinessQueryService queryService;
    private final CurrentUserService currentUserService;

    public BusinessController(
        BusinessApplicationService businessApplicationService,
        WorkspaceBusinessQueryService queryService,
        CurrentUserService currentUserService
    ) {
        this.businessApplicationService = businessApplicationService;
        this.queryService = queryService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/workspaces/{workspaceId}/businesses")
    public ResponseEntity<BusinessResponse> createBusiness(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody BusinessCreateRequest request,
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        BusinessTemplateReference templateReference =
            new BusinessTemplateReference(
                request.templateId(),
                request.templateVersion()
            );

        var business = businessApplicationService.createBusiness(
            user.id(),
            new WorkspaceId(workspaceId),
            templateReference,
            request.name(),
            request.description(),
            request.configuration()
        );

        BusinessResponse response = BusinessResponse.from(business);

        return ResponseEntity
            .created(URI.create("/api/v1/businesses/" + response.id()))
            .body(response);
    }

    @GetMapping("/workspaces/{workspaceId}/businesses")
    public List<BusinessResponse> getBusinesses(
        @PathVariable UUID workspaceId,
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        return queryService.getBusinesses(
                new WorkspaceId(workspaceId),
                user.id()
            )
            .stream()
            .map(BusinessResponse::from)
            .toList();
    }

    @GetMapping("/businesses/{businessId}")
    public BusinessResponse getBusiness(
        @PathVariable UUID businessId,
        Authentication authentication
    ) {
        User user = currentUserService.requireActiveUser(
            authentication.getName()
        );

        var business = queryService.getBusiness(
            new BusinessId(businessId),
            user.id()
        );

        return BusinessResponse.from(business);
    }
}
