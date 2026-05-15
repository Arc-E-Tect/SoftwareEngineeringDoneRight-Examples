package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Domain model representing the inner token issued by the Authorization Service.
 *
 * <p>The inner token carries RBAC and ABAC information as standard JWT
 * claims.  It is attached to outbound requests from the MFA to the
 * associated microservice.
 *
 * @param tokenValue  the signed JWT string
 * @param claims      decoded claims map (role assignments, attributes, etc.)
 * @param expiresAt   expiry instant of this token
 */
public record InnerToken(String tokenValue, Map<String, Object> claims, Instant expiresAt) {

    /**
     * Returns {@code true} when the inner token has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Convenience accessor – returns the value of a single claim, or
     * {@code null} when the claim is absent.
     */
    public Object getClaim(String name) {
        return claims.get(name);
    }
}
