package com.businesslab.platform.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevelopmentEndpointGuardIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    DevelopmentEndpointGuardIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldRequireAuthenticationForWorkspaceEndpoints()
        throws Exception {

        mockMvc.perform(get("/api/v1/workspaces"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void shouldRequireAuthenticationForBusinessDetailEndpoint()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/v1/businesses/{businessId}",
                    UUID.randomUUID()
                )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void shouldRequireAuthenticationForWorkspaceBusinessListing()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/v1/workspaces/{workspaceId}/businesses",
                    UUID.randomUUID()
                )
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("AUTHENTICATION_REQUIRED"));
    }
}
