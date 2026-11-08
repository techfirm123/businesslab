package com.businesslab.platform.system.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private static final String APPLICATION_NAME = "businesslab-api";

    @GetMapping("/status")
    public SystemStatusResponse status() {
        return new SystemStatusResponse(
            APPLICATION_NAME,
            "UP"
        );
    }
}
