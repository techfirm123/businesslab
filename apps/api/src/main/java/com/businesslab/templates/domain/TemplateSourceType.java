
package com.businesslab.templates.domain;

/**
 * Describes how a template's behavior is supplied by the platform.
 */
public enum TemplateSourceType {

    /**
     * Template behavior is implemented by platform-owned Java code.
     */
    CODE_BACKED,

    /**
     * Template behavior is described by a constrained, versioned
     * definition interpreted by approved platform engines.
     */
    DECLARATIVE
}
