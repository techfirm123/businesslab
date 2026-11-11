
package com.businesslab.templates.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplateApiIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    TemplateApiIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldExposeAvailableCoffeeShopTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].templateId")
                .value("coffee-shop"))
            .andExpect(jsonPath("$[0].version")
                .value("1.0.0"))
            .andExpect(jsonPath("$[0].sourceType")
                .value("CODE_BACKED"))
            .andExpect(jsonPath("$[0].status")
                .value("ACTIVE"))
            .andExpect(jsonPath("$[0].configurationFields.length()")
                .value(2));
    }
}
