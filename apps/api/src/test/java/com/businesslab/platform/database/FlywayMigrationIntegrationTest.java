
package com.businesslab.platform.database;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationIntegrationTest {

    private static final String HISTORY_TABLE_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC'
          AND UPPER(TABLE_NAME) = 'FLYWAY_SCHEMA_HISTORY'
        """;

    private static final String APPLICATION_TABLE_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC'
          AND UPPER(TABLE_NAME) IN (
              'WORKSPACE',
              'BUSINESS_TEMPLATE',
              'BUSINESS_TEMPLATE_VERSION',
              'BUSINESS',
              'BUSINESS_CONFIGURATION'
          )
        """;

    private static final String IDENTITY_TABLE_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC'
          AND UPPER(TABLE_NAME) IN (
              'APP_USER',
              'USER_PASSWORD_CREDENTIAL'
          )
        """;

    private static final String SESSION_TABLE_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC'
          AND UPPER(TABLE_NAME) IN (
              'SPRING_SESSION',
              'SPRING_SESSION_ATTRIBUTES'
          )
        """;

    private static final String MEMBERSHIP_TABLE_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE UPPER(TABLE_SCHEMA) = 'PUBLIC'
          AND UPPER(TABLE_NAME) = 'WORKSPACE_MEMBERSHIP'
        """;

    private static final String COFFEE_SHOP_VERSION_COUNT_QUERY = """
        SELECT COUNT(*)
        FROM BUSINESS_TEMPLATE_VERSION
        WHERE TEMPLATE_ID = 'coffee-shop'
          AND VERSION = '1.0.0'
          AND SOURCE_TYPE = 'CODE_BACKED'
          AND LIFECYCLE_STATUS = 'PUBLISHED'
        """;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    FlywayMigrationIntegrationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void shouldCreateFlywaySchemaHistoryTable() {
        Integer tableCount = jdbcTemplate.queryForObject(
            HISTORY_TABLE_COUNT_QUERY,
            Integer.class
        );

        assertThat(tableCount)
            .isNotNull()
            .isEqualTo(1);
    }

    @Test
    void shouldCreateGenericBusinessPersistenceTables() {
        Integer tableCount = jdbcTemplate.queryForObject(
            APPLICATION_TABLE_COUNT_QUERY,
            Integer.class
        );

        assertThat(tableCount)
            .isNotNull()
            .isEqualTo(5);
    }

    @Test
    void shouldCreateUserIdentityTables() {
        Integer tableCount = jdbcTemplate.queryForObject(
            IDENTITY_TABLE_COUNT_QUERY,
            Integer.class
        );

        assertThat(tableCount)
            .isNotNull()
            .isEqualTo(2);
    }

    @Test
    void shouldCreatePersistentSessionTables() {
        Integer tableCount = jdbcTemplate.queryForObject(
            SESSION_TABLE_COUNT_QUERY,
            Integer.class
        );

        assertThat(tableCount)
            .isNotNull()
            .isEqualTo(2);
    }

    @Test
    void shouldCreateWorkspaceMembershipTable() {
        Integer tableCount = jdbcTemplate.queryForObject(
            MEMBERSHIP_TABLE_COUNT_QUERY,
            Integer.class
        );

        assertThat(tableCount)
            .isNotNull()
            .isEqualTo(1);
    }

    @Test
    void shouldSeedTheExactCoffeeShopTemplateVersion() {
        Integer versionCount = jdbcTemplate.queryForObject(
            COFFEE_SHOP_VERSION_COUNT_QUERY,
            Integer.class
        );

        assertThat(versionCount)
            .isNotNull()
            .isEqualTo(1);
    }

    @Test
    void shouldAllowDatabaseQueriesAfterFlywayInitialization() {
        Integer result = jdbcTemplate.queryForObject(
            "SELECT 1",
            Integer.class
        );

        assertThat(result).isEqualTo(1);
    }
}
