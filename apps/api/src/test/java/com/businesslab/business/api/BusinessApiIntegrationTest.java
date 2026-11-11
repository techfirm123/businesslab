
package com.businesslab.business.api;

import com.businesslab.business.application.BusinessApplicationService;
import com.businesslab.workspace.domain.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "businesslab.api.development-endpoints-enabled=true"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BusinessApiIntegrationTest {

    private final MockMvc mockMvc;
    private final BusinessApplicationService businessApplicationService;
    private final JdbcTemplate jdbcTemplate;

    private final Set<UUID> createdWorkspaceIds = new HashSet<>();

    @Autowired
    BusinessApiIntegrationTest(
        MockMvc mockMvc,
        BusinessApplicationService businessApplicationService,
        JdbcTemplate jdbcTemplate
    ) {
        this.mockMvc = mockMvc;
        this.businessApplicationService = businessApplicationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @AfterEach
    void cleanUpCreatedWorkspaces() {
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
                "DELETE FROM workspace WHERE workspace_id = ?",
                workspaceId
            );
        }

        createdWorkspaceIds.clear();
    }

    @Test
    void shouldCreateAndRetrieveWorkspace() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/workspaces")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "name": "API Integration Workspace"
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name")
                .value("API Integration Workspace"))
            .andExpect(jsonPath("$.storageMode")
                .value("CLOUD"))
            .andExpect(jsonPath("$.status")
                .value("ACTIVE"))
            .andReturn();

        String location = createResult.getResponse()
            .getHeader("Location");

        assertThat(location).isNotBlank();

        UUID workspaceId = UUID.fromString(
            location.substring(location.lastIndexOf('/') + 1)
        );

        createdWorkspaceIds.add(workspaceId);

        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(workspaceId.toString()))
            .andExpect(jsonPath("$.name")
                .value("API Integration Workspace"));
    }

    @Test
    void shouldRejectWorkspaceRequestWithBlankName() throws Exception {
        mockMvc.perform(
                post("/api/v1/workspaces")
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "name": " "
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("REQUEST_VALIDATION_FAILED"))
            .andExpect(jsonPath("$.details[0].field")
                .value("name"));
    }

    @Test
    void shouldReturnNotFoundForUnknownWorkspace() throws Exception {
        UUID unknownWorkspaceId = UUID.randomUUID();

        mockMvc.perform(
                get("/api/v1/workspaces/{workspaceId}", unknownWorkspaceId)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnNotFoundForUnknownBusiness() throws Exception {
        UUID unknownBusinessId = UUID.randomUUID();

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", unknownBusinessId)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldCreateRetrieveAndListBusinesses() throws Exception {
        Workspace workspace =
            businessApplicationService.createCloudWorkspace(
                "Business API Workspace"
            );

        createdWorkspaceIds.add(workspace.id().value());

        MvcResult createResult = mockMvc.perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspace.id().value()
                )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "templateId": "coffee-shop",
                          "templateVersion": "1.0.0",
                          "name": "API Coffee Shop",
                          "description": "Created through the BusinessLab API",
                          "configuration": {
                            "initialCapital": 125000.50,
                            "currency": "INR"
                          }
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name")
                .value("API Coffee Shop"))
            .andExpect(jsonPath("$.workspaceId")
                .value(workspace.id().toString()))
            .andExpect(jsonPath("$.templateId")
                .value("coffee-shop"))
            .andExpect(jsonPath("$.templateVersion")
                .value("1.0.0"))
            .andExpect(jsonPath("$.status")
                .value("DRAFT"))
            .andExpect(jsonPath("$.configuration.currency")
                .value("INR"))
            .andReturn();

        String location = createResult.getResponse()
            .getHeader("Location");

        assertThat(location).isNotBlank();

        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name")
                .value("API Coffee Shop"))
            .andExpect(jsonPath("$.templateVersion")
                .value("1.0.0"));

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspace.id().value()
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name")
                .value("API Coffee Shop"));
    }

    @Test
    void shouldReturnTemplateConfigurationErrors() throws Exception {
        Workspace workspace =
            businessApplicationService.createCloudWorkspace(
                "Invalid Business API Workspace"
            );

        createdWorkspaceIds.add(workspace.id().value());

        mockMvc.perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    workspace.id().value()
                )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "templateId": "coffee-shop",
                          "templateVersion": "1.0.0",
                          "name": "Invalid Coffee Shop",
                          "description": "Missing currency",
                          "configuration": {
                            "initialCapital": 50000
                          }
                        }
                        """)
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
    void shouldReturnNotFoundWhenCreatingBusinessInUnknownWorkspace()
        throws Exception {

        UUID unknownWorkspaceId = UUID.randomUUID();

        mockMvc.perform(
                post(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    unknownWorkspaceId
                )
                    .contentType(APPLICATION_JSON)
                    .content("""
                        {
                          "templateId": "coffee-shop",
                          "templateVersion": "1.0.0",
                          "name": "Unknown Workspace Business",
                          "configuration": {
                            "initialCapital": 50000,
                            "currency": "INR"
                          }
                        }
                        """)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code")
                .value("RESOURCE_NOT_FOUND"));
    }
}
