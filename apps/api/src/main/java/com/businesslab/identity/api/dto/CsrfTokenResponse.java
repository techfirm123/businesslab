
package com.businesslab.identity.api.dto;

public record CsrfTokenResponse(
    String token,
    String headerName,
    String parameterName
) {
}
