
package com.businesslab.workspace.api;

import com.businesslab.identity.application.UserRegistrationService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceOwnershipApiIntegrationTest {

    private static final String SESSION_COOKIE =
        "BUSINESSLAB_SESSION";

    private static final String VALID_PASSWORD =
        "BusinessLab-Strong-2026";

    private final MockMvc mockMvc;
    private final UserRegistrationService registrationService;
    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    private final Set<String> createdEmails = new HashSet<>();
    private final Set<String> createdSessionIds = new HashSet<>();
    private final Set<UUID> createdWorkspaceIds = new HashSet<>();

    @Autowired
    WorkspaceOwnershipApiIntegrationTest(
        MockMvc mockMvc,
        UserRegistrationService registrationService,
        JdbcTemplate jdbcTemplate,
        JsonMapper jsonMapper
    ) {
        this.mockMvc = mockMvc;
        this.registrationService = registrationService;
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    @AfterEach
    void cleanUp() {
        for (UUID workspaceId : createdWorkspaceIds) {
            jdbcTemplate.update(
                "DELETE FROM workspace WHERE workspace_id = ?",
                workspaceId
            );
        }

        for (String sessionId : createdSessionIds) {
            jdbcTemplate.update(
                "DELETE FROM SPRING_SESSION WHERE SESSION_ID = ?",
                sessionId
            );
        }

        for (String email : createdEmails) {
            jdbcTemplate.update(
                "DELETE FROM app_user WHERE email = ?",
                email
            );
        }

        createdWorkspaceIds.clear();
        createdSessionIds.clear();
        createdEmails.clear();
    }

    @Test
    void shouldRejectAnonymousWorkspaceListing() throws Exception {
        mockMvc.perform(get("/api/v1/workspaces"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void shouldCreateWorkspaceWithOwnerMembershipAndListIt()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin(
            "Workspace Owner"
        );

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Owner Workspace"
        );

        Integer membershipCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM workspace_membership
            WHERE workspace_id = ?
              AND user_id = (
                  SELECT user_id
                  FROM app_user
                  WHERE email = ?
              )
              AND role = 'OWNER'
              AND status = 'ACTIVE'
            """,
            Integer.class,
            workspaceId,
            owner.email()
        );

        assertThat(membershipCount).isEqualTo(1);

        mockMvc.perform(
                get("/api/v1/workspaces")
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id")
                .value(workspaceId.toString()))
            .andExpect(jsonPath("$[0].name")
                .value("Owner Workspace"))
            .andExpect(jsonPath("$[0].membershipRole")
                .value("OWNER"))
            .andExpect(jsonPath("$[0].membershipStatus")
                .value("ACTIVE"));

        mockMvc.perform(
                get("/api/v1/workspaces/{workspaceId}", workspaceId)
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(workspaceId.toString()))
            .andExpect(jsonPath("$.membershipRole")
                .value("OWNER"));
    }

    @Test
    void shouldPreventAnotherUserFromAccessingWorkspace()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin(
            "First Workspace Owner"
        );

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Private Workspace"
        );

        AuthenticatedUser anotherUser = registerAndLogin(
            "Another User"
        );

        mockMvc.perform(
                get("/api/v1/workspaces/{workspaceId}", workspaceId)
                    .cookie(anotherUser.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(
                get("/api/v1/workspaces")
                    .cookie(anotherUser.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectAccessThroughSuspendedMembership()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin(
            "Suspended Membership Owner"
        );

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Suspended Workspace"
        );

        jdbcTemplate.update(
            """
            UPDATE workspace_membership
            SET status = 'SUSPENDED'
            WHERE workspace_id = ?
              AND user_id = (
                  SELECT user_id
                  FROM app_user
                  WHERE email = ?
              )
            """,
            workspaceId,
            owner.email()
        );

        mockMvc.perform(
                get("/api/v1/workspaces/{workspaceId}", workspaceId)
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(
                get("/api/v1/workspaces")
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectWorkspaceCreationWithoutAuthentication()
        throws Exception {

        CsrfSession csrfSession = obtainCsrfSession(null);

        mockMvc.perform(
                post("/api/v1/workspaces")
                    .cookie(csrfSession.sessionCookie())
                    .header(
                        csrfSession.headerName(),
                        csrfSession.token()
                    )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "name": "Anonymous Workspace"
                        }
                        """)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }

    private AuthenticatedUser registerAndLogin(
        String displayName
    ) throws Exception {
        String email = newTestEmail();

        registrationService.register(
            email,
            displayName,
            VALID_PASSWORD
        );

        createdEmails.add(email);

        CsrfSession csrfSession = obtainCsrfSession(null);

        MvcResult loginResult = mockMvc.perform(
                post("/api/v1/auth/login")
                    .cookie(csrfSession.sessionCookie())
                    .header(
                        csrfSession.headerName(),
                        csrfSession.token()
                    )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, VALID_PASSWORD))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andReturn();

        Cookie authenticatedCookie = loginResult
            .getResponse()
            .getCookie(SESSION_COOKIE);

        assertThat(authenticatedCookie).isNotNull();
        assertThat(authenticatedCookie.isHttpOnly()).isTrue();

        createdSessionIds.add(authenticatedCookie.getValue());

        return new AuthenticatedUser(
            email,
            authenticatedCookie
        );
    }

    private UUID createWorkspace(
        Cookie authenticatedCookie,
        String workspaceName
    ) throws Exception {
        CsrfSession csrfSession = obtainCsrfSession(
            authenticatedCookie
        );

        MvcResult result = mockMvc.perform(
                post("/api/v1/workspaces")
                    .cookie(csrfSession.sessionCookie())
                    .header(
                        csrfSession.headerName(),
                        csrfSession.token()
                    )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s"
                        }
                        """.formatted(workspaceName))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(workspaceName))
            .andExpect(jsonPath("$.storageMode").value("CLOUD"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.membershipRole").value("OWNER"))
            .andExpect(jsonPath("$.membershipStatus").value("ACTIVE"))
            .andReturn();

        String location = result.getResponse().getHeader("Location");

        assertThat(location).isNotBlank();

        UUID workspaceId = UUID.fromString(
            location.substring(location.lastIndexOf('/') + 1)
        );

        createdWorkspaceIds.add(workspaceId);

        return workspaceId;
    }

    private CsrfSession obtainCsrfSession(
        Cookie existingSessionCookie
    ) throws Exception {
        MockHttpServletRequestBuilder request =
            get("/api/v1/auth/csrf");

        if (existingSessionCookie != null) {
            request.cookie(existingSessionCookie);
        }

        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.headerName")
                .value("X-CSRF-TOKEN"))
            .andReturn();

        Cookie sessionCookie = result
            .getResponse()
            .getCookie(SESSION_COOKIE);

        if (sessionCookie == null) {
            sessionCookie = existingSessionCookie;
        }

        assertThat(sessionCookie).isNotNull();

        createdSessionIds.add(sessionCookie.getValue());

        Map<?, ?> response = jsonMapper
            .readerFor(Map.class)
            .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .readValue(result.getResponse().getContentAsString());

        String token = (String) response.get("token");
        String headerName = (String) response.get("headerName");

        assertThat(token).isNotBlank();
        assertThat(headerName).isNotBlank();

        return new CsrfSession(
            token,
            headerName,
            sessionCookie
        );
    }

    private String newTestEmail() {
        return (
            "bl015-workspace-"
                + UUID.randomUUID()
                + "@example.com"
        ).toLowerCase(Locale.ROOT);
    }

    private record AuthenticatedUser(
        String email,
        Cookie sessionCookie
    ) {
    }

    private record CsrfSession(
        String token,
        String headerName,
        Cookie sessionCookie
    ) {
    }
}
