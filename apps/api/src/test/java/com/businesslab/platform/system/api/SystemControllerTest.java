package com.businesslab.platform.system.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemControllerTest {

    private final SystemController systemController =
        new SystemController();

    @Test
    void statusReturnsApplicationIdentity() {
        SystemStatusResponse response = systemController.status();

        assertThat(response.application())
            .isEqualTo("businesslab-api");

        assertThat(response.status())
            .isEqualTo("UP");
    }
}
