
package com.businesslab.platform.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "businesslab.api.development-endpoints-enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevelopmentEndpointGuardIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    DevelopmentEndpointGuardIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldRegisterWorkspaceEndpointsAndRequireAuthentication()
        throws Exception {

        mockMvc.perform(get("/api/v1/workspaces"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldKeepBusinessDevelopmentEndpointsDisabled()
        throws Exception {

        mockMvc.perform(
                get("/api/v1/businesses/{businessId}", java.util.UUID.randomUUID())
                    .with(user("development-test@example.com"))
            )
            .andExpect(status().isNotFound());
    }
}
