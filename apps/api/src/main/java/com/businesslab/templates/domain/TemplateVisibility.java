
package com.businesslab.templates.domain;

/**
 * Controls where a template may be discovered.
 */
public enum TemplateVisibility {

    /**
     * Platform-owned catalog template.
     */
    PLATFORM,

    /**
     * Visible only within its owning workspace.
     */
    PRIVATE,

    /**
     * Shared with an explicitly defined audience.
     */
    SHARED,

    /**
     * Publicly discoverable after platform publication policy is satisfied.
     */
    PUBLIC
}
