package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter;

/**
 * Represents the scope granted by a validated API key.
 *
 * <p>The API Gateway encodes the scope in the configurable
 * {@code mfe-adapter.api-gateway.scope-header} after successful key
 * validation.  The MFA reads this header and stores the resolved scope on
 * the {@link com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest}
 * so downstream components (validators, transformers, the microservice
 * client) can make scope-aware decisions.
 *
 * <p>Matching is case-insensitive: the header value {@code "read"} maps to
 * {@link #READ}, {@code "WRITE"} maps to {@link #WRITE}, etc.
 */
public enum ApiKeyScope {

    /** Grants read access (safe HTTP methods: GET, HEAD, OPTIONS). */
    READ,

    /** Grants read and write access (all HTTP methods). */
    WRITE,

    /** Grants read-only access to audit / reporting endpoints. */
    AUDIT;

    /**
     * Parses the given string value to a scope, ignoring case.
     *
     * @param value the header value supplied by the API Gateway
     * @return the matching scope, or {@code null} if the value does not match
     *         any known scope
     */
    public static ApiKeyScope fromHeaderValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
