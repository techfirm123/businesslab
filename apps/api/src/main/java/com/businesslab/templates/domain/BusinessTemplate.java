package com.businesslab.templates.domain;

import com.businesslab.business.domain.BusinessConfiguration;

/**
 * Contract implemented by individual business templates.
 *
 * Implementations define their own:
 *
 * - Template metadata
 * - Supported capabilities
 * - Configuration fields
 * - Configuration validation rules
 *
 * Simulation mechanics and business-specific operations
 * will be added through subsequent template extensions.
 */
public interface BusinessTemplate {

    /**
     * Returns the immutable metadata describing this
     * exact template version.
     */
    BusinessTemplateDescriptor descriptor();

    /**
     * Validates a business configuration according to
     * this template's requirements.
     */
    BusinessTemplateValidationResult validateConfiguration(
        BusinessConfiguration configuration
    );
}
