package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.identity.application.UserRegistrationService;
import com.businesslab.identity.domain.User;
import com.businesslab.workspace.application.WorkspaceApplicationService;
import com.businesslab.workspace.domain.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(
    named = "BUSINESSLAB_RUN_POSTGRES_TESTS",
    matches = "true"
)
class PostgresBusinessPersistenceIntegrationTest {

    private static final BusinessTemplateReference COFFEE_SHOP =
        new BusinessTemplateReference("coffee-shop", "1.0.0");

    private static final String VALID_PASSWORD =
        "BusinessLab-Strong-2026";

    private final BusinessApplicationService businessApplicationService;
    private final WorkspaceApplicationService workspaceApplicationService;
    private final UserRegistrationService userRegistrationService;
    private final BusinessRepository businessRepository;
    private final JdbcTemplate jdbcTemplate;

    private final Set<UUID> createdWorkspaceIds = new HashSet<>();
    private final Set<String> createdEmails = new HashSet<>();

    @Autowired
    PostgresBusinessPersistenceIntegrationTest(
        BusinessApplicationService businessApplicationService,
        WorkspaceApplicationService workspaceApplicationService,
        UserRegistrationService userRegistrationService,
        BusinessRepository businessRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.businessApplicationService = businessApplicationService;
        this.workspaceApplicationService = workspaceApplicationService;
        this.userRegistrationService = userRegistrationService;
        this.businessRepository = businessRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldPersistAndRestoreBusinessConfigurationUsingPostgresJsonb() {
        User owner = createOwner();

        Workspace workspace = createOwnedWorkspace(
            owner,
            "PostgreSQL Integration Workspace"
        );

        Business created = businessApplicationService.createBusiness(
            owner.id(),
            workspace.id(),
            COFFEE_SHOP,
            "PostgreSQL Integration Coffee Shop",
            "Validates PostgreSQL JSONB persistence",
            Map.of(
                "initialCapital",
                new BigDecimal("987654321.123456"),
                "currency",
                "INR"
            )
        );

        Business restored = businessRepository
            .findById(created.id())
            .orElseThrow();

        assertThat(restored.templateReference())
            .isEqualTo(COFFEE_SHOP);

        assertThat(restored.configuration().get("currency"))
            .isEqualTo("INR");

        assertThat(
            (BigDecimal) restored.configuration().get("initialCapital")
        ).isEqualByComparingTo(
            new BigDecimal("987654321.123456")
        );

        Integer configurationRowCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM business_configuration
            WHERE business_id = ?
            """,
            Integer.class,
            restored.id().value()
        );

        assertThat(configurationRowCount)
            .isEqualTo(1);
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
    }

    private User createOwner() {
        String email = (
            "business-postgres-"
                + UUID.randomUUID()
                + "@example.com"
        ).toLowerCase(Locale.ROOT);

        User user = userRegistrationService.register(
            email,
            "PostgreSQL Business Owner",
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
}
