
package com.businesslab.platform.shared.application;

/**
 * Raised when an application resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(
        String resourceType,
        String identifier
    ) {
        super(resourceType + " not found: " + identifier);
    }
}
