
package com.businesslab.templates.domain;

/**
 * Governance lifecycle for declarative template versions.
 *
 * Published and deprecated versions are immutable. Editing a template
 * creates a new version.
 */
public enum DeclarativeTemplateLifecycleStatus {

    DRAFT,

    VALIDATING,

    APPROVED,

    PUBLISHED,

    DEPRECATED,

    REJECTED
}
