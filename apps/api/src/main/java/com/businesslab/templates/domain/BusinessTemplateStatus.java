package com.businesslab.templates.domain;

/**
 * Availability status of a business template version.
 *
 * ACTIVE:
 * Available for creating new businesses.
 *
 * DEPRECATED:
 * Cannot be selected for new businesses, but remains
 * available for existing businesses using this version.
 */
public enum BusinessTemplateStatus {

    ACTIVE,

    DEPRECATED
}
