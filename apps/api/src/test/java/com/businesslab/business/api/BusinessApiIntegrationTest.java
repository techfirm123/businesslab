package com.businesslab.business.api;

import com.businesslab.identity.application.UserRegistrationService;
import com.businesslab.identity.domain.User;
import com.businesslab.workspace.application.WorkspaceMembershipRepository;
import com.businesslab.workspace.domain.WorkspaceId;
import com.businesslab.workspace.domain.WorkspaceMembership;
import com.businesslab.workspace.domain.WorkspaceMembershipRole;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
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
class BusinessApiIntegrationTest {

    private static final String SESSION_COOKIE =
        "BUSINESSLAB_SESSION";

    private static final String VALID_PASSWORD =
        "BusinessLab-Strong-2026";

    private final MockMvc mockMvc;
    private final UserRegistrationService registrationService;
    private final WorkspaceMembershipRepository membershipRepository;
    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    private final Set<String> createdEmails = new HashSet<>();
    private final Set<String> createdSessionIds = new HashSet<>();
    private final Set<UUID> createdWorkspaceIds = new HashSet<>();

    @Autowired
    BusinessApiIntegrationTest(
        MockMvc mockMvc,
        UserRegistrationService registrationService,
        WorkspaceMembershipRepository membershipRepository,
        JdbcTemplate jdbcTemplate,
        JsonMapper jsonMapper
    ) {
        this.mockMvc = mockMvc;
        this.registrationService = registrationService;
        this.membershipRepository = membershipRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    @AfterEach
    void cleanUp() {
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
    void shouldCreateRetrieveAndListBusinessesForWorkspaceOwner()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Business Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Owner Business Workspace"
        );

        UUID businessId = createBusiness(
            owner,
            workspaceId,
            "Owner Coffee Shop"
        );

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(businessId.toString()))
            .andExpect(jsonPath("$.workspaceId")
                .value(workspaceId.toString()))
            .andExpect(jsonPath("$.name").value("Owner Coffee Shop"))
            .andExpect(jsonPath("$.templateId").value("coffee-shop"))
            .andExpect(jsonPath("$.templateVersion").value("1.0.0"));

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(businessId.toString()))
            .andExpect(jsonPath("$[0].name").value("Owner Coffee Shop"));
    }

    @Test
    void shouldAllowActiveMembersToCreateAndReadBusinesses()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Member Test Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Member Access Workspace"
        );

        AuthenticatedUser member = registerAndLogin("Business Member");

        addMembership(
            workspaceId,
            member,
            WorkspaceMembershipRole.MEMBER
        );

        UUID businessId = createBusiness(
            member,
            workspaceId,
            "Member Created Coffee Shop"
        );

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(member.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(businessId.toString()));

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
                    .cookie(member.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name")
                .value("Member Created Coffee Shop"));
    }

    @Test
    void shouldAllowViewersToReadButNotCreateBusinesses()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Viewer Test Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Viewer Access Workspace"
        );

        UUID businessId = createBusiness(
            owner,
            workspaceId,
            "Owner Created Coffee Shop"
        );

        AuthenticatedUser viewer = registerAndLogin("Business Viewer");

        addMembership(
            workspaceId,
            viewer,
            WorkspaceMembershipRole.VIEWER
        );

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(viewer.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(businessId.toString()));

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
                    .cookie(viewer.sessionCookie())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(businessId.toString()));

        submitBusiness(
            viewer,
            workspaceId,
            "Viewer Must Not Create This Business"
        )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code")
                .value("WORKSPACE_PERMISSION_DENIED"));
    }

    @Test
    void shouldConcealBusinessesFromUsersWithoutWorkspaceMembership()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Private Workspace Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Private Business Workspace"
        );

        UUID businessId = createBusiness(
            owner,
            workspaceId,
            "Private Coffee Shop"
        );

        AuthenticatedUser outsider = registerAndLogin("Business Outsider");

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(outsider.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
                    .cookie(outsider.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message")
                .value("Business not found: " + businessId));

        submitBusiness(
            outsider,
            workspaceId,
            "Outsider Must Not Create This Business"
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldConcealBusinessesFromUsersWithSuspendedMembership()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Suspension Test Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Suspended Member Workspace"
        );

        UUID businessId = createBusiness(
            owner,
            workspaceId,
            "Workspace Coffee Shop"
        );

        AuthenticatedUser member = registerAndLogin("Suspended Business Member");

        addMembership(
            workspaceId,
            member,
            WorkspaceMembershipRole.MEMBER
        );

        WorkspaceMembership membership = membershipRepository
            .findByWorkspaceIdAndUserId(workspaceId(workspaceId), member.user().id())
            .orElseThrow();

        membershipRepository.update(
            membership.suspend(Instant.now().plusSeconds(1))
        );

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(member.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
                    .cookie(member.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));

        submitBusiness(
            member,
            workspaceId,
            "Suspended Member Must Not Create This Business"
        )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnNotFoundForUnknownBusinessForWorkspaceMember()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Unknown Business Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Unknown Business Workspace"
        );

        UUID unknownBusinessId = UUID.randomUUID();

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", unknownBusinessId)
                    .cookie(owner.sessionCookie())
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message")
                .value("Business not found: " + unknownBusinessId));
    }

    @Test
    void shouldPreserveBusinessTemplateConfigurationValidation()
        throws Exception {

        AuthenticatedUser owner = registerAndLogin("Validation Test Owner");

        UUID workspaceId = createWorkspace(
            owner.sessionCookie(),
            "Business Validation Workspace"
        );

        submitRawBusiness(
            owner,
            workspaceId,
            """
            {
              "templateId": "coffee-shop",
              "templateVersion": "1.0.0",
              "name": "Invalid Coffee Shop",
              "description": "Missing currency",
              "configuration": {
                "initialCapital": 50000
              }
            }
            """
        )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.code")
                .value("BUSINESS_CONFIGURATION_INVALID"))
            .andExpect(jsonPath("$.details[0].field")
                .value("currency"))
            .andExpect(jsonPath("$.details[0].code")
                .value("REQUIRED"));
    }

    @Test
    void shouldRequireAuthenticationForBusinessReadsAndCreation()
        throws Exception {

        UUID workspaceId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", businessId)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));

        CsrfSession csrfSession = obtainCsrfSession(null);

        mockMvc.perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspaceId
                )
                    .cookie(csrfSession.sessionCookie())
                    .header(
                        csrfSession.headerName(),
                        csrfSession.token()
                    )
                    .contentType(APPLICATION_JSON)
                    .content(businessPayload("Anonymous Business"))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }

    private UUID createBusiness(
        AuthenticatedUser user,
        UUID workspaceId,
        String businessName
    ) throws Exception {
        MvcResult result = submitBusiness(
            user,
            workspaceId,
            businessName
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(businessName))
            .andExpect(jsonPath("$.workspaceId")
                .value(workspaceId.toString()))
            .andExpect(jsonPath("$.templateId").value("coffee-shop"))
            .andExpect(jsonPath("$.templateVersion").value("1.0.0"))
            .andReturn();

        Map<?, ?> response = jsonMapper
            .readerFor(Map.class)
            .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .readValue(result.getResponse().getContentAsString());

        return UUID.fromString((String) response.get("id"));
    }

    private ResultActions submitBusiness(
        AuthenticatedUser user,
        UUID workspaceId,
        String businessName
    ) throws Exception {
        return submitRawBusiness(
            user,
            workspaceId,
            businessPayload(businessName)
        );
    }

    private ResultActions submitRawBusiness(
        AuthenticatedUser user,
        UUID workspaceId,
        String payload
    ) throws Exception {
        CsrfSession csrfSession = obtainCsrfSession(
            user.sessionCookie()
        );

        return mockMvc.perform(
            post(
                "/api/v1/workspaces/{workspaceId}/businesses",
                workspaceId
            )
                .cookie(csrfSession.sessionCookie())
                .header(
                    csrfSession.headerName(),
                    csrfSession.token()
                )
                .contentType(APPLICATION_JSON)
                .content(payload)
        );
    }

    private String businessPayload(String businessName) {
        return """
            {
              "templateId": "coffee-shop",
              "templateVersion": "1.0.0",
              "name": "%s",
              "description": "Created through the BusinessLab API",
              "configuration": {
                "initialCapital": 125000.50,
                "currency": "INR"
              }
            }
            """.formatted(businessName);
    }

    private void addMembership(
        UUID workspaceId,
        AuthenticatedUser user,
        WorkspaceMembershipRole role
    ) {
        membershipRepository.save(
            WorkspaceMembership.createMember(
                workspaceId(workspaceId),
                user.user().id(),
                role,
                Instant.now()
            )
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

    private AuthenticatedUser registerAndLogin(
        String displayName
    ) throws Exception {
        String email = newTestEmail();

        User user = registrationService.register(
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
            user,
            authenticatedCookie
        );
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
            "bl015-business-"
                + UUID.randomUUID()
                + "@example.com"
        ).toLowerCase(Locale.ROOT);
    }

    private WorkspaceId workspaceId(UUID value) {
        return new WorkspaceId(value);
    }

    private record AuthenticatedUser(
        User user,
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
