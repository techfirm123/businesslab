
package com.businesslab.workspace.infrastructure.persistence;

import com.businesslab.workspace.application.WorkspaceRepository;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceStatus;
import com.businesslab.workspace.domain.WorkspaceStorageMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcWorkspaceRepository implements WorkspaceRepository {

    private static final String INSERT_SQL = """
        INSERT INTO workspace (
            workspace_id,
            name,
            storage_mode,
            status,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT
            workspace_id,
            name,
            storage_mode,
            status,
            created_at,
            updated_at
        FROM workspace
        WHERE workspace_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcWorkspaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(Workspace workspace) {
        if (workspace.storageMode() != WorkspaceStorageMode.CLOUD) {
            throw new IllegalArgumentException(
                "Only cloud workspaces can be persisted by the API"
            );
        }

        jdbcTemplate.update(
            INSERT_SQL,
            workspace.id().value(),
            workspace.name(),
            workspace.storageMode().name(),
            workspace.status().name(),
            Timestamp.from(workspace.createdAt()),
            Timestamp.from(workspace.updatedAt())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Workspace> findById(WorkspaceId workspaceId) {
        return jdbcTemplate.query(
                FIND_BY_ID_SQL,
                (resultSet, rowNumber) -> Workspace.reconstitute(
                    new WorkspaceId(
                        UUID.fromString(resultSet.getString("workspace_id"))
                    ),
                    resultSet.getString("name"),
                    WorkspaceStorageMode.valueOf(
                        resultSet.getString("storage_mode")
                    ),
                    WorkspaceStatus.valueOf(
                        resultSet.getString("status")
                    ),
                    resultSet.getTimestamp("created_at").toInstant(),
                    resultSet.getTimestamp("updated_at").toInstant()
                ),
                workspaceId.value()
            )
            .stream()
            .findFirst();
    }
}
