
package com.businesslab.workspace.infrastructure.persistence;

import com.businesslab.identity.domain.UserId;
import com.businesslab.workspace.application.WorkspaceMembershipRepository;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceMembership;
import com.businesslab.workspace.domain.WorkspaceMembershipId;
import com.businesslab.workspace.domain.WorkspaceMembershipRole;
import com.businesslab.workspace.domain.WorkspaceMembershipStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcWorkspaceMembershipRepository
    implements WorkspaceMembershipRepository {

    private static final String INSERT_SQL = """
        INSERT INTO workspace_membership (
            membership_id,
            workspace_id,
            user_id,
            role,
            status,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE workspace_membership
        SET
            role = ?,
            status = ?,
            updated_at = ?
        WHERE membership_id = ?
          AND workspace_id = ?
          AND user_id = ?
        """;

    private static final String SELECT_COLUMNS = """
        SELECT
            membership_id,
            workspace_id,
            user_id,
            role,
            status,
            created_at,
            updated_at
        FROM workspace_membership
        """;

    private static final String FIND_BY_WORKSPACE_AND_USER_SQL =
        SELECT_COLUMNS
            + " WHERE workspace_id = ? AND user_id = ?";

    private static final String FIND_ACTIVE_BY_USER_SQL =
        SELECT_COLUMNS
            + " WHERE user_id = ? AND status = 'ACTIVE'"
            + " ORDER BY created_at DESC, membership_id";

    private final JdbcTemplate jdbcTemplate;

    public JdbcWorkspaceMembershipRepository(
        JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(WorkspaceMembership membership) {
        jdbcTemplate.update(
            INSERT_SQL,
            membership.id().value(),
            membership.workspaceId().value(),
            membership.userId().value(),
            membership.role().name(),
            membership.status().name(),
            Timestamp.from(membership.createdAt()),
            Timestamp.from(membership.updatedAt())
        );
    }

    @Override
    @Transactional
    public void update(WorkspaceMembership membership) {
        int updatedRows = jdbcTemplate.update(
            UPDATE_SQL,
            membership.role().name(),
            membership.status().name(),
            Timestamp.from(membership.updatedAt()),
            membership.id().value(),
            membership.workspaceId().value(),
            membership.userId().value()
        );

        if (updatedRows != 1) {
            throw new IllegalStateException(
                "Workspace membership could not be updated: "
                    + membership.id()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(
        WorkspaceId workspaceId,
        UserId userId
    ) {
        return jdbcTemplate.query(
                FIND_BY_WORKSPACE_AND_USER_SQL,
                this::mapMembership,
                workspaceId.value(),
                userId.value()
            )
            .stream()
            .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMembership> findActiveByUserId(
        UserId userId
    ) {
        return jdbcTemplate.query(
            FIND_ACTIVE_BY_USER_SQL,
            this::mapMembership,
            userId.value()
        );
    }

    private WorkspaceMembership mapMembership(
        java.sql.ResultSet resultSet,
        int rowNumber
    ) throws java.sql.SQLException {

        return WorkspaceMembership.reconstitute(
            new WorkspaceMembershipId(
                UUID.fromString(resultSet.getString("membership_id"))
            ),
            new WorkspaceId(
                UUID.fromString(resultSet.getString("workspace_id"))
            ),
            new UserId(
                UUID.fromString(resultSet.getString("user_id"))
            ),
            WorkspaceMembershipRole.valueOf(
                resultSet.getString("role")
            ),
            WorkspaceMembershipStatus.valueOf(
                resultSet.getString("status")
            ),
            resultSet.getTimestamp("created_at").toInstant(),
            resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}
