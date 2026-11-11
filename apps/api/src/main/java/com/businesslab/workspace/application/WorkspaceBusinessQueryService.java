package com.businesslab.workspace.application;

import com.businesslab.business.application.BusinessRepository;
import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.identity.domain.UserId;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.templates.application.BusinessTemplateRegistry;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.workspace.domain.WorkspaceId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Read operations for businesses and template discovery.
 *
 * Business data is always read through an authenticated user's
 * active workspace membership.
 */
@Service
public class WorkspaceBusinessQueryService {

    private final BusinessRepository businessRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final BusinessTemplateRegistry templateRegistry;

    public WorkspaceBusinessQueryService(
        BusinessRepository businessRepository,
        WorkspaceAccessService workspaceAccessService,
        BusinessTemplateRegistry templateRegistry
    ) {
        this.businessRepository = Objects.requireNonNull(
            businessRepository,
            "Business repository is required"
        );
        this.workspaceAccessService = Objects.requireNonNull(
            workspaceAccessService,
            "Workspace access service is required"
        );
        this.templateRegistry = Objects.requireNonNull(
            templateRegistry,
            "Template registry is required"
        );
    }

    /**
     * Retrieves a business only when the requesting user has an
     * active membership in the business's owning workspace.
     *
     * An inaccessible business is reported as not found. The response
     * does not disclose the owning workspace ID.
     */
    @Transactional(readOnly = true)
    public Business getBusiness(
        BusinessId businessId,
        UserId requestingUserId
    ) {
        Objects.requireNonNull(
            businessId,
            "Business ID is required"
        );
        Objects.requireNonNull(
            requestingUserId,
            "Requesting user ID is required"
        );

        Business business = businessRepository.findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Business",
                businessId.toString()
            ));

        try {
            workspaceAccessService.requireActiveMembership(
                business.workspaceId(),
                requestingUserId
            );
        } catch (ResourceNotFoundException exception) {
            /*
             * Conceal the owning workspace when the caller has no
             * active membership. Unknown and inaccessible businesses
             * therefore have the same resource-level response.
             */
            throw new ResourceNotFoundException(
                "Business",
                businessId.toString()
            );
        }

        return business;
    }

    /**
     * Lists businesses only for a workspace in which the requesting
     * user has an active membership. All current active roles are
     * permitted to read workspace businesses.
     */
    @Transactional(readOnly = true)
    public List<Business> getBusinesses(
        WorkspaceId workspaceId,
        UserId requestingUserId
    ) {
        Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );
        Objects.requireNonNull(
            requestingUserId,
            "Requesting user ID is required"
        );

        workspaceAccessService.requireActiveMembership(
            workspaceId,
            requestingUserId
        );

        return businessRepository.findByWorkspaceId(workspaceId);
    }

    /**
     * Template discovery remains public and does not expose workspace data.
     */
    @Transactional(readOnly = true)
    public List<BusinessTemplateDescriptor> getAvailableTemplates() {
        return templateRegistry.findAvailable();
    }
}
