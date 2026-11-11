
package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessConfiguration;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.templates.application.BusinessTemplateRegistry;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateStatus;
import com.businesslab.templates.domain.BusinessTemplateValidationResult;
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
 * Coordinates workspace and business creation.
 *
 * This service currently creates cloud-persisted workspaces only.
 * Browser-local workspaces will remain in the frontend's IndexedDB path.
 *
 * Business creation currently supports registered code-backed templates.
 * Declarative templates are persisted as definitions but will become
 * executable only after the declarative interpreter and validator exist.
 */
@Service
public class BusinessApplicationService {

    private final WorkspaceRepository workspaceRepository;
    private final BusinessRepository businessRepository;
    private final BusinessTemplateRegistry templateRegistry;
    private final Clock clock;

    public BusinessApplicationService(
        WorkspaceRepository workspaceRepository,
        BusinessRepository businessRepository,
        BusinessTemplateRegistry templateRegistry,
        Clock clock
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
            "Business template registry is required"
        );
        this.clock = Objects.requireNonNull(
            clock,
            "Clock is required"
        );
    }

    /**
     * Creates a new cloud workspace.
     */
    @Transactional
    public Workspace createCloudWorkspace(String name) {
        Instant now = currentInstant();

        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            name,
            WorkspaceStorageMode.CLOUD,
            now
        );

        workspaceRepository.save(workspace);

        return workspace;
    }

    /**
     * Creates a business in an existing cloud workspace.
     */
    @Transactional
    public Business createBusiness(
        WorkspaceId workspaceId,
        BusinessTemplateReference templateReference,
        String name,
        String description,
        Map<String, ?> configurationValues
    ) {
        Objects.requireNonNull(
            workspaceId,
            "Workspace ID is required"
        );

        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Workspace not found: " + workspaceId
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

    /**
     * Creates a cloud workspace and its first business atomically.
     *
     * If template resolution or configuration validation fails,
     * the workspace insert is rolled back with the transaction.
     */
    @Transactional
    public WorkspaceBusinessCreationResult createWorkspaceWithBusiness(
        String workspaceName,
        String businessName,
        String businessDescription,
        BusinessTemplateReference templateReference,
        Map<String, ?> configurationValues
    ) {
        Instant now = currentInstant();

        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            workspaceName,
            WorkspaceStorageMode.CLOUD,
            now
        );

        workspaceRepository.save(workspace);

        Business business = createBusinessInWorkspace(
            workspace,
            templateReference,
            businessName,
            businessDescription,
            configurationValues,
            now
        );

        return new WorkspaceBusinessCreationResult(
            workspace,
            business
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
            .orElseThrow(() -> new IllegalArgumentException(
                "Business template version not found: "
                    + templateReference.templateId()
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
