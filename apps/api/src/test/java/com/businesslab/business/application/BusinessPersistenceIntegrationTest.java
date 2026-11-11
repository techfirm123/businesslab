package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.identity.application.UserRegistrationService;
import com.businesslab.identity.domain.User;
import com.businesslab.platform.shared.application.ResourceNotFoundException;
import com.businesslab.templates.domain.BusinessTemplateValidationError;
import com.businesslab.workspace.application.WorkspaceApplicationService;
import com.businesslab.workspace.application.WorkspaceRepository;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceStorageMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BusinessPersistenceIntegrationTest {

    private static final BusinessTemplateReference COFFEE_SHOP =
        new BusinessTemplateReference("coffee-shop", "1.0.0");

    private static final String VALID_PASSWORD =
        "BusinessLab-Strong-2026";

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceApplicationService workspaceApplicationService;
    private final UserRegistrationService userRegistrationService;
    private final WorkspaceRepository workspaceRepository;
    private final BusinessRepository businessRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Set<UUID> createdWorkspaceIds = new HashSet<>();
    private final Set<String> createdEmails = new HashSet<>();

    @Autowired
    BusinessPersistenceIntegrationTest(
        BusinessApplicationService businessApplicationService,
        WorkspaceApplicationService workspaceApplicationService,
        UserRegistrationService userRegistrationService,
        WorkspaceRepository workspaceRepository,
        BusinessRepository businessRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.businessApplicationService = businessApplicationService;
        this.workspaceApplicationService = workspaceApplicationService;
        this.userRegistrationService = userRegistrationService;
        this.workspaceRepository = workspaceRepository;
        this.businessRepository = businessRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @AfterEach
    void cleanUpCreatedRecords() {
        for (UUID workspaceId : createdWorkspaceIds) {
            jdbcTemplate.update(
                """
                DELETE FROM business_configuration
                WHERE business_id IN (
                    SELECT business_id
                    FROM business
                    WHERE workspace_id = ?
                )
                """,
                workspaceId
            );

            jdbcTemplate.update(
                "DELETE FROM business WHERE workspace_id = ?",
                workspaceId
            );

            jdbcTemplate.update(
                "DELETE FROM workspace_membership WHERE workspace_id = ?",
                workspaceId
            );

            jdbcTemplate.update(
                "DELETE FROM workspace WHERE workspace_id = ?",
                workspaceId
            );
        }

        for (String email : createdEmails) {
            jdbcTemplate.update(
                "DELETE FROM app_user WHERE email = ?",
                email
            );
        }

        createdWorkspaceIds.clear();
        createdEmails.clear();
    }

    @Test
    void shouldPersistWorkspaceAndBusinessWithExactTemplateVersion() {
        User owner = createOwner();

        Workspace workspace = createOwnedWorkspace(
            owner,
            "My Business Workspace"
        );

        Business createdBusiness =
            businessApplicationService.createBusiness(
                owner.id(),
                workspace.id(),
                COFFEE_SHOP,
                "My First Coffee Shop",
                "Initial coffee shop business",
                Map.of(
                    "initialCapital",
                    new BigDecimal("125000.50"),
                    "currency",
                    "INR"
                )
            );

        Workspace persistedWorkspace = workspaceRepository
            .findById(workspace.id())
            .orElseThrow();

        Business persistedBusiness = businessRepository
            .findById(createdBusiness.id())
            .orElseThrow();

        assertThat(persistedWorkspace.id())
            .isEqualTo(workspace.id());

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

        assertThat(
            businessRepository.findByWorkspaceId(persistedWorkspace.id())
        ).hasSize(1);
    }

    @Test
    void shouldCreateBusinessInAnExistingOwnedCloudWorkspace() {
        User owner = createOwner();

        Workspace workspace = createOwnedWorkspace(
            owner,
            "Existing Owned Workspace"
        );

        Business business = businessApplicationService.createBusiness(
            owner.id(),
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
    void shouldNotPersistBusinessWhenConfigurationIsInvalid() {
        User owner = createOwner();

        Workspace workspace = createOwnedWorkspace(
            owner,
            "Configuration Validation Workspace"
        );

        long businessesBefore = countBusinesses(workspace.id());

        assertThatThrownBy(
            () -> businessApplicationService.createBusiness(
                owner.id(),
                workspace.id(),
                COFFEE_SHOP,
                "Invalid Coffee Shop",
                "Missing required currency",
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

        assertThat(countBusinesses(workspace.id()))
            .isEqualTo(businessesBefore);
    }

    @Test
    void shouldNotPersistBusinessWhenTemplateVersionDoesNotExist() {
        User owner = createOwner();

        Workspace workspace = createOwnedWorkspace(
            owner,
            "Unknown Template Workspace"
        );

        long businessesBefore = countBusinesses(workspace.id());

        BusinessTemplateReference unknownTemplate =
            new BusinessTemplateReference(
                "unknown-template",
                "1.0.0"
            );

        assertThatThrownBy(
            () -> businessApplicationService.createBusiness(
                owner.id(),
                workspace.id(),
                unknownTemplate,
                "Unknown Template Business",
                "",
                Map.of()
            )
        )
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Business template version not found");

        assertThat(countBusinesses(workspace.id()))
            .isEqualTo(businessesBefore);
    }

    private User createOwner() {
        String email = (
            "business-persistence-"
                + UUID.randomUUID()
                + "@example.com"
        ).toLowerCase(Locale.ROOT);

        User user = userRegistrationService.register(
            email,
            "Business Persistence Owner",
            VALID_PASSWORD
        );

        createdEmails.add(email);

        return user;
    }

    private Workspace createOwnedWorkspace(
        User owner,
        String workspaceName
    ) {
        var details = workspaceApplicationService.createCloudWorkspace(
            workspaceName,
            owner.id()
        );

        createdWorkspaceIds.add(details.workspace().id().value());

        return details.workspace();
    }

    private long countBusinesses(WorkspaceId workspaceId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM business WHERE workspace_id = ?",
            Long.class,
            workspaceId.value()
        );

        return count == null ? 0L : count;
    }
}
