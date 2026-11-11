
package com.businesslab.workspace.application;

import com.businesslab.business.application.BusinessRepository;
import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.templates.application.BusinessTemplateRegistry;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Read operations for workspaces, businesses and template discovery.
 */
@Service
public class WorkspaceBusinessQueryService {

    private final WorkspaceRepository workspaceRepository;
    private final BusinessRepository businessRepository;
    private final BusinessTemplateRegistry templateRegistry;

    public WorkspaceBusinessQueryService(
        WorkspaceRepository workspaceRepository,
        BusinessRepository businessRepository,
        BusinessTemplateRegistry templateRegistry
    ) {
        this.workspaceRepository = Objects.requireNonNull(
            workspaceRepository,
            "Workspace repository is required"
        );
        this.businessRepository = Objects.requireNonNull(
            businessRepository,
            "Business repository is required"
        );
        this.templateRegistry = Objects.requireNonNull(
            templateRegistry,
            "Template registry is required"
        );
    }

    @Transactional(readOnly = true)
    public Workspace getWorkspace(WorkspaceId workspaceId) {
        Objects.requireNonNull(workspaceId, "Workspace ID is required");

        return workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Workspace",
                workspaceId.toString()
            ));
    }

    @Transactional(readOnly = true)
    public Business getBusiness(BusinessId businessId) {
        Objects.requireNonNull(businessId, "Business ID is required");

        return businessRepository.findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Business",
                businessId.toString()
            ));
    }

    @Transactional(readOnly = true)
    public List<Business> getBusinesses(WorkspaceId workspaceId) {
        getWorkspace(workspaceId);

        return businessRepository.findByWorkspaceId(workspaceId);
    }

    @Transactional(readOnly = true)
    public List<BusinessTemplateDescriptor> getAvailableTemplates() {
        return templateRegistry.findAvailable();
    }
}
