
package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.templates.domain.BusinessTemplateValidationError;
import com.businesslab.workspace.application.WorkspaceRepository;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceStorageMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BusinessPersistenceIntegrationTest {

    private static final BusinessTemplateReference COFFEE_SHOP =
        new BusinessTemplateReference("coffee-shop", "1.0.0");

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceRepository workspaceRepository;
    private final BusinessRepository businessRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    BusinessPersistenceIntegrationTest(
        BusinessApplicationService businessApplicationService,
        WorkspaceRepository workspaceRepository,
        BusinessRepository businessRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.businessApplicationService = businessApplicationService;
        this.workspaceRepository = workspaceRepository;
        this.businessRepository = businessRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldPersistWorkspaceAndBusinessWithExactTemplateVersion() {
        WorkspaceBusinessCreationResult created =
            businessApplicationService.createWorkspaceWithBusiness(
                "My Business Workspace",
                "My First Coffee Shop",
                "Initial coffee shop business",
                COFFEE_SHOP,
                Map.of(
                    "initialCapital",
                    new BigDecimal("125000.50"),
                    "currency",
                    "INR"
                )
            );

        Workspace persistedWorkspace = workspaceRepository
            .findById(created.workspace().id())
            .orElseThrow();

        Business persistedBusiness = businessRepository
            .findById(created.business().id())
            .orElseThrow();

        assertThat(persistedWorkspace.id())
            .isEqualTo(created.workspace().id());

        assertThat(persistedWorkspace.storageMode())
            .isEqualTo(WorkspaceStorageMode.CLOUD);

        assertThat(persistedBusiness.workspaceId())
            .isEqualTo(persistedWorkspace.id());

        assertThat(persistedBusiness.templateReference())
            .isEqualTo(COFFEE_SHOP);

        assertThat(persistedBusiness.name())
            .isEqualTo("My First Coffee Shop");

        assertThat(persistedBusiness.description())
            .isEqualTo("Initial coffee shop business");

        assertThat(persistedBusiness.configuration().get("currency"))
            .isEqualTo("INR");

        assertThat(
            (BigDecimal) persistedBusiness.configuration()
                .get("initialCapital")
        ).isEqualByComparingTo(new BigDecimal("125000.50"));

        assertThat(businessRepository.findByWorkspaceId(persistedWorkspace.id()))
            .hasSize(1);
    }

    @Test
    void shouldCreateBusinessInAnExistingCloudWorkspace() {
        Workspace workspace =
            businessApplicationService.createCloudWorkspace(
                "Existing Workspace"
            );

        Business business = businessApplicationService.createBusiness(
            workspace.id(),
            COFFEE_SHOP,
            "Second Coffee Shop",
            "Business created in an existing workspace",
            Map.of(
                "initialCapital",
                new BigDecimal("250000"),
                "currency",
                "INR"
            )
        );

        assertThat(business.workspaceId())
            .isEqualTo(workspace.id());

        assertThat(businessRepository.findById(business.id()))
            .isPresent();
    }

    @Test
    void shouldRollbackWorkspaceWhenBusinessConfigurationIsInvalid() {
        long workspaceCountBefore = countWorkspaces();

        assertThatThrownBy(
            () -> businessApplicationService.createWorkspaceWithBusiness(
                "Workspace Should Roll Back",
                "Invalid Coffee Shop",
                "Missing required currency",
                COFFEE_SHOP,
                Map.of(
                    "initialCapital",
                    new BigDecimal("50000")
                )
            )
        )
            .isInstanceOf(BusinessConfigurationValidationException.class)
            .satisfies(exception -> {
                BusinessConfigurationValidationException validation =
                    (BusinessConfigurationValidationException) exception;

                assertThat(validation.errors())
                    .extracting(BusinessTemplateValidationError::fieldKey)
                    .contains("currency");
            });

        assertThat(countWorkspaces())
            .isEqualTo(workspaceCountBefore);
    }

    @Test
    void shouldRollbackWorkspaceWhenTemplateVersionDoesNotExist() {
        long workspaceCountBefore = countWorkspaces();

        BusinessTemplateReference unknownTemplate =
            new BusinessTemplateReference(
                "unknown-template",
                "1.0.0"
            );

        assertThatThrownBy(
            () -> businessApplicationService.createWorkspaceWithBusiness(
                "Workspace Should Roll Back",
                "Unknown Template Business",
                "",
                unknownTemplate,
                Map.of()
            )
        )
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Business template version not found");

        assertThat(countWorkspaces())
            .isEqualTo(workspaceCountBefore);
    }

    private long countWorkspaces() {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM workspace",
            Long.class
        );

        return count == null ? 0L : count;
    }
}
