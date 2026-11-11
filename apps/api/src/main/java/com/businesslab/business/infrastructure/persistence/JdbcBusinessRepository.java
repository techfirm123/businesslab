
package com.businesslab.business.infrastructure.persistence;

import com.businesslab.business.application.BusinessRepository;
import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessConfiguration;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.business.domain.BusinessStatus;
import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.workspace.domain.WorkspaceId;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL/H2 JDBC implementation of business persistence.
 *
 * PostgreSQL uses JSONB. H2's test profile uses JSON.
 * The SQL type is determined from the active JDBC connection.
 */
@Repository
public class JdbcBusinessRepository implements BusinessRepository {

    private static final String INSERT_BUSINESS_SQL = """
        INSERT INTO business (
            business_id,
            workspace_id,
            template_id,
            template_version,
            name,
            description,
            status,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERT_CONFIGURATION_SQL_PREFIX = """
        INSERT INTO business_configuration (
            business_id,
            configuration,
            schema_version,
            updated_at
        ) VALUES (?, CAST(? AS
        """;

    private static final String INSERT_CONFIGURATION_SQL_SUFFIX = """
        ), 1, ?)
        """;

    private static final String SELECT_BUSINESS_COLUMNS = """
        SELECT
            b.business_id,
            b.workspace_id,
            b.template_id,
            b.template_version,
            b.name,
            b.description,
            b.status,
            b.created_at,
            b.updated_at,
            c.configuration,
            c.schema_version
        FROM business b
        INNER JOIN business_configuration c
            ON c.business_id = b.business_id
        """;

    private static final String FIND_BY_ID_SQL =
        SELECT_BUSINESS_COLUMNS + " WHERE b.business_id = ?";

    private static final String FIND_BY_WORKSPACE_SQL =
        SELECT_BUSINESS_COLUMNS
            + " WHERE b.workspace_id = ?"
            + " ORDER BY b.created_at DESC, b.business_id";

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;
    private final String jsonColumnType;

    public JdbcBusinessRepository(
        JdbcTemplate jdbcTemplate,
        JsonMapper jsonMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
        this.jsonColumnType = detectJsonColumnType(jdbcTemplate);
    }

    @Override
    @Transactional
    public void save(Business business) {
        jdbcTemplate.update(
            INSERT_BUSINESS_SQL,
            business.id().value(),
            business.workspaceId().value(),
            business.templateReference().templateId(),
            business.templateReference().version(),
            business.name(),
            business.description(),
            business.status().name(),
            Timestamp.from(business.createdAt()),
            Timestamp.from(business.updatedAt())
        );

        String configurationJson = serializeConfiguration(
            business.configuration()
        );

        String insertConfigurationSql =
            INSERT_CONFIGURATION_SQL_PREFIX
                + jsonColumnType
                + INSERT_CONFIGURATION_SQL_SUFFIX;

        jdbcTemplate.update(
            insertConfigurationSql,
            business.id().value(),
            configurationJson,
            Timestamp.from(business.updatedAt())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Business> findById(BusinessId businessId) {
        return jdbcTemplate.query(
                FIND_BY_ID_SQL,
                this::mapBusiness,
                businessId.value()
            )
            .stream()
            .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Business> findByWorkspaceId(WorkspaceId workspaceId) {
        return jdbcTemplate.query(
            FIND_BY_WORKSPACE_SQL,
            this::mapBusiness,
            workspaceId.value()
        );
    }

    private Business mapBusiness(
        java.sql.ResultSet resultSet,
        int rowNumber
    ) throws SQLException {

        int schemaVersion = resultSet.getInt("schema_version");

        if (schemaVersion != 1) {
            throw new SQLException(
                "Unsupported business configuration schema version: "
                    + schemaVersion
            );
        }

        BusinessConfiguration configuration =
            readConfiguration(resultSet.getString("configuration"));

        return Business.reconstitute(
            new BusinessId(
                UUID.fromString(resultSet.getString("business_id"))
            ),
            new WorkspaceId(
                UUID.fromString(resultSet.getString("workspace_id"))
            ),
            new BusinessTemplateReference(
                resultSet.getString("template_id"),
                resultSet.getString("template_version")
            ),
            resultSet.getString("name"),
            resultSet.getString("description"),
            configuration,
            BusinessStatus.valueOf(resultSet.getString("status")),
            resultSet.getTimestamp("created_at").toInstant(),
            resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    private String serializeConfiguration(
        BusinessConfiguration configuration
    ) {
        try {
            return jsonMapper.writeValueAsString(configuration.values());
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                "Could not serialize business configuration",
                exception
            );
        }
    }

    /**
     * Reads configuration JSON from either PostgreSQL JSONB or H2 JSON.
     *
     * Some JDBC JSON representations return the JSON document directly.
     * Others can return a JSON string containing the document itself.
     * Both representations are normalized before mapping.
     */
    private BusinessConfiguration readConfiguration(
        String configurationJson
    ) throws SQLException {

        if (configurationJson == null || configurationJson.isBlank()) {
            throw new SQLException(
                "Stored business configuration is empty"
            );
        }

        try {
            String normalizedJson = configurationJson.trim();

            // Unwrap a JSON string that contains the actual JSON document.
            if (normalizedJson.startsWith("\"")) {
                normalizedJson = jsonMapper
                    .readerFor(String.class)
                    .readValue(normalizedJson);
            }

            Map<?, ?> decoded = jsonMapper
                .readerFor(Map.class)
                .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .readValue(normalizedJson);

            Map<String, Object> configuration = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : decoded.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new SQLException(
                        "Business configuration contains a non-string key"
                    );
                }

                configuration.put(key, entry.getValue());
            }

            return BusinessConfiguration.of(configuration);

        } catch (JacksonException exception) {
            throw new SQLException(
                "Could not deserialize business configuration JSON",
                exception
            );
        } catch (IllegalArgumentException exception) {
            throw new SQLException(
                "Stored business configuration is invalid",
                exception
            );
        }
    }

    private static String detectJsonColumnType(
        JdbcTemplate jdbcTemplate
    ) {
        String databaseProduct = jdbcTemplate.execute(
            (ConnectionCallback<String>) connection ->
                connection.getMetaData().getDatabaseProductName()
        );

        if (databaseProduct == null) {
            throw new IllegalStateException(
                "Could not determine database product for JSON persistence"
            );
        }

        String normalized = databaseProduct.toLowerCase(Locale.ROOT);

        if (normalized.contains("postgresql")) {
            return "JSONB";
        }

        if (normalized.contains("h2")) {
            return "JSON";
        }

        throw new IllegalStateException(
            "Unsupported database for JSON persistence: " + databaseProduct
        );
    }
}
