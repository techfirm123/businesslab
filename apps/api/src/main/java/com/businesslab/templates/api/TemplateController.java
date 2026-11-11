
package com.businesslab.templates.api;

import com.businesslab.templates.api.dto.BusinessTemplateResponse;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;
import com.businesslab.workspace.application.WorkspaceBusinessQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only business template discovery API.
 */
@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final WorkspaceBusinessQueryService queryService;

    public TemplateController(
        WorkspaceBusinessQueryService queryService
    ) {
        this.queryService = queryService;
    }

    @GetMapping
    public List<BusinessTemplateResponse> findAvailableTemplates() {
        List<BusinessTemplateDescriptor> descriptors =
            queryService.getAvailableTemplates();

        return descriptors.stream()
            .map(BusinessTemplateResponse::from)
            .toList();
    }
}
