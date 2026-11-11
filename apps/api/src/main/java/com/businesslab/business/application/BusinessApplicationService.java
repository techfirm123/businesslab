package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessConfiguration;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.identity.domain.UserId;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.templates.application.BusinessTemplateRegistry;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import com.businesslab.templates.domain.BusinessTemplateValidationResult;
import com.businesslab.workspace.application.WorkspaceAccessService;
import com.businesslab.workspace.application.WorkspaceRepository;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceStatus;
import com.businesslab.workspace.domain.WorkspaceStorageMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates business creation within an existing workspace.
 *
 * Every business creation operation is scoped to the authenticated
 * user's active workspace membership. Workspace creation itself is
 * owned by WorkspaceApplicationService.
 *
 * Business creation currently supports registered code-backed templates.
 * Declarative templates will become usable after the declarative
 * interpreter and validator have been implemented.
 */
@Service
public class BusinessApplicationService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final BusinessRepository businessRepository;
    private final BusinessTemplateRegistry templateRegistry;
    private final Clock clock;

    public BusinessApplicationService(
        WorkspaceRepository workspaceRepository,
        WorkspaceAccessService workspaceAccessService,
        BusinessRepository businessRepository,
        BusinessTemplateRegistry templateRegistry,
        Clock clock
    ) {
        this.workspaceRepository = Objects.requireNonNull(
            workspaceRepository,
            "Workspace repository is required"
        );
        this.workspaceAccessService = Objects.requireNonNull(
            workspaceAccessService,
            "Workspace access service is required"
        );
        this.businessRepository = Objects.requireNonNull(
            businessRepository,
            "Business repository is required"
        );
        this.templateRegistry = Objects.requireNonNull(
            templateRegistry,
            "Business template registry is required"
        );
        this.clock = Objects.requireNonNull(
            clock,
            "Clock is required"
        );
    }

    /**
     * Creates a business in an existing cloud workspace.
     *
     * The requesting user must have an active membership and a role
     * that permits business creation.
     */
    @Transactional
    public Business createBusiness(
        UserId requestingUserId,
        WorkspaceId workspaceId,
        BusinessTemplateReference templateReference,
        String name,
        String description,
        Map<String, ?> configurationValues
    ) {
        Objects.requireNonNull(
            requestingUserId,
            "Requesting user ID is required"
        );
        Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );

        /*
         * Authorize before loading the workspace. A user who does not
         * belong to the workspace must not learn whether it exists.
         */
        workspaceAccessService.requireBusinessCreationAccess(
            workspaceId,
            requestingUserId
        );

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Workspace",
                workspaceId.toString()
            ));

        return createBusinessInWorkspace(
            workspace,
            templateReference,
            name,
            description,
            configurationValues,
            currentInstant()
        );
    }

    private Business createBusinessInWorkspace(
        Workspace workspace,
        BusinessTemplateReference templateReference,
        String name,
        String description,
        Map<String, ?> configurationValues,
        Instant createdAt
    ) {
        Objects.requireNonNull(
            templateReference,
            "Business template reference is required"
        );

        if (workspace.status() != WorkspaceStatus.ACTIVE) {
            throw new IllegalStateException(
                "Businesses cannot be created in archived workspaces"
            );
        }

        if (workspace.storageMode() != WorkspaceStorageMode.CLOUD) {
            throw new IllegalArgumentException(
                "The API can create businesses only in cloud workspaces"
            );
        }

        BusinessTemplate template = templateRegistry.find(templateReference)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Business template version",
                templateReference.templateId()
                    + "@"
                    + templateReference.version()
            ));

        if (template.descriptor().status() != BusinessTemplateStatus.ACTIVE) {
            throw new IllegalStateException(
                "Deprecated templates cannot be used to create new businesses: "
                    + templateReference.templateId()
                    + "@"
                    + templateReference.version()
            );
        }

        BusinessConfiguration configuration =
            BusinessConfiguration.of(configurationValues);

        BusinessTemplateValidationResult validation =
            template.validateConfiguration(configuration);

        if (!validation.isValid()) {
            throw new BusinessConfigurationValidationException(
                validation.errors()
            );
        }

        Business business = Business.create(
            BusinessId.generate(),
            workspace.id(),
            templateReference,
            name,
            description,
            configuration,
            createdAt
        );

        businessRepository.save(business);

        return business;
    }

    private Instant currentInstant() {
        // PostgreSQL TIMESTAMPTZ is stored at microsecond precision.
        return Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
    }
}
