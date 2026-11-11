
package com.businesslab.identity.infrastructure.persistence;

import com.businesslab.identity.application.UserRepository;
import com.businesslab.identity.domain.EmailAddress;
import com.businesslab.identity.domain.User;
import com.businesslab.identity.domain.UserId;
import com.businesslab.identity.domain.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String INSERT_SQL = """
        INSERT INTO app_user (
            user_id,
            email,
            display_name,
            status,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT
            user_id,
            email,
            display_name,
            status,
            created_at,
            updated_at
        FROM app_user
        WHERE user_id = ?
        """;

    private static final String FIND_BY_EMAIL_SQL = """
        SELECT
            user_id,
            email,
            display_name,
            status,
            created_at,
            updated_at
        FROM app_user
        WHERE email = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(User user) {
        jdbcTemplate.update(
            INSERT_SQL,
            user.id().value(),
            user.email().value(),
            user.displayName(),
            user.status().name(),
            Timestamp.from(user.createdAt()),
            Timestamp.from(user.updatedAt())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId userId) {
        return jdbcTemplate.query(
                FIND_BY_ID_SQL,
                this::mapUser,
                userId.value()
            )
            .stream()
            .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(EmailAddress email) {
        return jdbcTemplate.query(
                FIND_BY_EMAIL_SQL,
                this::mapUser,
                email.value()
            )
            .stream()
            .findFirst();
    }

    private User mapUser(
        java.sql.ResultSet resultSet,
        int rowNumber
    ) throws java.sql.SQLException {

        return User.reconstitute(
            new UserId(
                UUID.fromString(resultSet.getString("user_id"))
            ),
            EmailAddress.of(resultSet.getString("email")),
            resultSet.getString("display_name"),
            UserStatus.valueOf(resultSet.getString("status")),
            resultSet.getTimestamp("created_at").toInstant(),
            resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}
