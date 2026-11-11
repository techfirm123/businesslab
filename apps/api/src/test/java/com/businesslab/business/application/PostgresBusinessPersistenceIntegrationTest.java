
package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.workspace.domain.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final BusinessApplicationService businessApplicationService;
    private final BusinessRepository businessRepository;
    private final JdbcTemplate jdbcTemplate;

    private final List<UUID> createdWorkspaceIds = new ArrayList<>();
    private final List<UUID> createdBusinessIds = new ArrayList<>();

    @Autowired
    PostgresBusinessPersistenceIntegrationTest(
        BusinessApplicationService businessApplicationService,
        BusinessRepository businessRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.businessApplicationService = businessApplicationService;
        this.businessRepository = businessRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldPersistAndRestoreBusinessConfigurationUsingPostgresJsonb() {
        WorkspaceBusinessCreationResult created =
            businessApplicationService.createWorkspaceWithBusiness(
                "PostgreSQL Integration Workspace",
                "PostgreSQL Integration Coffee Shop",
                "Validates PostgreSQL JSONB persistence",
                COFFEE_SHOP,
                Map.of(
                    "initialCapital",
                    new BigDecimal("987654321.123456"),
                    "currency",
                    "INR"
                )
            );

        createdWorkspaceIds.add(created.workspace().id().value());
        createdBusinessIds.add(created.business().id().value());

        Business restored = businessRepository
            .findById(created.business().id())
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
        for (UUID businessId : createdBusinessIds) {
            jdbcTemplate.update(
                "DELETE FROM business_configuration WHERE business_id = ?",
                businessId
            );

            jdbcTemplate.update(
                "DELETE FROM business WHERE business_id = ?",
                businessId
            );
        }

        for (UUID workspaceId : createdWorkspaceIds) {
            jdbcTemplate.update(
                "DELETE FROM workspace WHERE workspace_id = ?",
                workspaceId
            );
        }
    }
}
