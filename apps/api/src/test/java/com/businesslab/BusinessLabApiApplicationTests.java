package com.businesslab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BusinessLabApiApplicationTests {

    @Test
    void applicationContextLoads() {
        // Verifies that the Spring Boot application context starts successfully.
    }
}
