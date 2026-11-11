
package com.businesslab.business.api;

import com.businesslab.business.api.dto.BusinessCreateRequest;
import com.businesslab.business.api.dto.BusinessResponse;
import com.businesslab.business.application.BusinessApplicationService;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.business.domain.BusinessId;
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
import java.util.List;
import java.util.UUID;

/**
 * Development-only business REST endpoints.
 *
 * These endpoints are disabled by default. They must not be exposed
 * in a production deployment before authentication and workspace
 * ownership authorization are implemented.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@ConditionalOnProperty(
    prefix = "businesslab.api",
    name = "development-endpoints-enabled",
    havingValue = "true"
)
public class BusinessController {

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceBusinessQueryService queryService;

    public BusinessController(
        BusinessApplicationService businessApplicationService,
        WorkspaceBusinessQueryService queryService
    ) {
        this.businessApplicationService = businessApplicationService;
        this.queryService = queryService;
    }

    @PostMapping("/workspaces/{workspaceId}/businesses")
    public ResponseEntity<BusinessResponse> createBusiness(
        @PathVariable UUID workspaceId,
        @Valid @RequestBody BusinessCreateRequest request
    ) {
        BusinessTemplateReference templateReference =
            new BusinessTemplateReference(
                request.templateId(),
                request.templateVersion()
            );

        var business = businessApplicationService.createBusiness(
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
        @PathVariable UUID workspaceId
    ) {
        return queryService.getBusinesses(new WorkspaceId(workspaceId))
            .stream()
            .map(BusinessResponse::from)
            .toList();
    }

    @GetMapping("/businesses/{businessId}")
    public BusinessResponse getBusiness(
        @PathVariable UUID businessId
    ) {
        var business = queryService.getBusiness(
            new BusinessId(businessId)
        );

        return BusinessResponse.from(business);
    }
}
