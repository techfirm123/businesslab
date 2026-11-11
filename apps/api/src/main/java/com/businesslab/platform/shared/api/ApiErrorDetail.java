
package com.businesslab.platform.shared.api;

/**
 * One field-level API error detail.
 */
public record ApiErrorDetail(
    String field,
    String code,
    String message
) {
}
