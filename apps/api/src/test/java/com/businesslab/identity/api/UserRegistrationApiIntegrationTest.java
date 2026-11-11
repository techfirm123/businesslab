
package com.businesslab.identity.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationApiIntegrationTest {

    private static final String VALID_PASSWORD =
        "BusinessLab-Strong-2026";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserRegistrationApiIntegrationTest(
        MockMvc mockMvc,
        JdbcTemplate jdbcTemplate,
        PasswordEncoder passwordEncoder
    ) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @AfterEach
    void cleanUpRegisteredUsers() {
        jdbcTemplate.update(
            """
            DELETE FROM app_user
            WHERE email LIKE 'bl015-test-%@example.com'
            """
        );
    }

    @Test
    void shouldRegisterUserAndPersistEncodedPassword() throws Exception {
        String email = newTestEmail();

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "displayName": "BusinessLab Tester",
                          "password": "%s"
                        }
                        """.formatted(email, VALID_PASSWORD))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").isNotEmpty())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.displayName")
                .value("BusinessLab Tester"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andExpect(jsonPath("$.password").doesNotExist());

        String encodedPassword = jdbcTemplate.queryForObject(
            """
            SELECT credential.password_hash
            FROM user_password_credential credential
            INNER JOIN app_user account
                ON account.user_id = credential.user_id
            WHERE account.email = ?
            """,
            String.class,
            email
        );

        assertThat(encodedPassword).isNotBlank();
        assertThat(encodedPassword).startsWith("{bcrypt}");
        assertThat(encodedPassword).isNotEqualTo(VALID_PASSWORD);
        assertThat(passwordEncoder.matches(
            VALID_PASSWORD,
            encodedPassword
        )).isTrue();
    }

    @Test
    void shouldNormalizeEmailBeforePersistingUser() throws Exception {
        String email = newTestEmail().toUpperCase(Locale.ROOT);

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "displayName": "Normalized User",
                          "password": "%s"
                        }
                        """.formatted(email, VALID_PASSWORD))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email")
                .value(email.toLowerCase(Locale.ROOT)));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM app_user WHERE email = ?",
            Integer.class,
            email.toLowerCase(Locale.ROOT)
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldRejectDuplicateEmailAddress() throws Exception {
        String email = newTestEmail();

        String requestBody = """
            {
              "email": "%s",
              "displayName": "Duplicate Tester",
              "password": "%s"
            }
            """.formatted(email, VALID_PASSWORD);

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "displayName": "Duplicate Tester Again",
                          "password": "%s"
                        }
                        """.formatted(email.toUpperCase(Locale.ROOT), VALID_PASSWORD))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code")
                .value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void shouldRejectPasswordShorterThanTwelveCharacters()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "displayName": "Weak Password User",
                          "password": "short"
                        }
                        """.formatted(newTestEmail()))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("REQUEST_VALIDATION_FAILED"));
    }

    @Test
    void shouldRejectPasswordLongerThanSeventyTwoUtf8Bytes()
        throws Exception {

        String email = newTestEmail();
        String oversizedPassword = "😀".repeat(19);

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "displayName": "Long Password User",
                          "password": "%s"
                        }
                        """.formatted(email, oversizedPassword))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("INVALID_ARGUMENT"));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM app_user WHERE email = ?",
            Integer.class,
            email.toLowerCase(Locale.ROOT)
        );

        assertThat(count).isZero();
    }

    @Test
    void shouldRejectInvalidEmailAddress() throws Exception {
        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "not-an-email",
                          "displayName": "Invalid Email User",
                          "password": "%s"
                        }
                        """.formatted(VALID_PASSWORD))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("REQUEST_VALIDATION_FAILED"));
    }

    private String newTestEmail() {
        return "bl015-test-" + UUID.randomUUID() + "@example.com";
    }
}
