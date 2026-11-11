package com.businesslab.templates.application;

import com.businesslab.business.domain.BusinessTemplateReference;
import com.businesslab.templates.domain.BusinessTemplate;
import com.businesslab.templates.domain.BusinessTemplateDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * Provides discovery and resolution of registered business templates.
 */
public interface BusinessTemplateRegistry {

    /**
     * Resolves a template using its exact identifier and version.
     *
     * Deprecated versions remain resolvable for existing businesses.
     */
    Optional<BusinessTemplate> find(
        BusinessTemplateReference reference
    );

    /**
     * Returns all currently active templates available
     * for creating new businesses.
     */
    List<BusinessTemplateDescriptor> findAvailable();
}
