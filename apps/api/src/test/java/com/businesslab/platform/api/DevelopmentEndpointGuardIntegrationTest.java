
package com.businesslab.platform.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void shouldNotRegisterWorkspaceEndpointsWhenDevelopmentApiIsDisabled()
        throws Exception {

        mockMvc.perform(get("/api/v1/workspaces/00000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotRegisterBusinessCreationEndpointWhenDevelopmentApiIsDisabled()
        throws Exception {

        mockMvc.perform(
                post("/api/v1/workspaces/00000000-0000-0000-0000-000000000001/businesses")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isNotFound());
    }
}
