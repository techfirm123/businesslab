
package com.businesslab.identity.infrastructure.persistence;

import com.businesslab.identity.application.PasswordCredentialRepository;
import com.businesslab.identity.domain.UserId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcPasswordCredentialRepository
    implements PasswordCredentialRepository {

    private static final String INSERT_SQL = """
        INSERT INTO user_password_credential (
            user_id,
            password_hash,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?)
        """;

    private static final String FIND_PASSWORD_SQL = """
        SELECT password_hash
        FROM user_password_credential
        WHERE user_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPasswordCredentialRepository(
        JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void save(
        UserId userId,
        String encodedPassword,
        Instant createdAt
    ) {
        jdbcTemplate.update(
            INSERT_SQL,
            userId.value(),
            encodedPassword,
            Timestamp.from(createdAt),
            Timestamp.from(createdAt)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findEncodedPasswordByUserId(UserId userId) {
        return jdbcTemplate.query(
                FIND_PASSWORD_SQL,
                (resultSet, rowNumber) ->
                    resultSet.getString("password_hash"),
                userId.value()
            )
            .stream()
            .findFirst();
    }
}
