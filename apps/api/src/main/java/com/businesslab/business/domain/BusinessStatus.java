package com.businesslab.business.domain;

/**
 * Lifecycle states of a virtual business.
 *
 * DRAFT:
 * Business has been created but has not started operating.
 *
 * ACTIVE:
 * Business is operating.
 *
 * PAUSED:
 * Business operations are temporarily suspended.
 *
 * CLOSED:
 * Business has permanently stopped operating.
 *
 * ARCHIVED:
 * Business has been archived and is read-only.
 */
public enum BusinessStatus {

    DRAFT,

    ACTIVE,

    PAUSED,

    CLOSED,

    ARCHIVED
}
