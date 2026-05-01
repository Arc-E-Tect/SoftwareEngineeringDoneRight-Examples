package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import java.time.Instant;

/**
 * Domain model representing the user token obtained from the OIDC Provider.
 *
 * <p>The token is an opaque or JWT value returned by the identity provider
 * after successful authentication.  The MFA stores it server-side and
 * presents it to IAS for swapping into an inner token.
 *
 * @param tokenValue  the raw token string (JWT or opaque)
 * @param expiresAt   expiry instant of the token
 * @param subject     identity of the authenticated user
 */
public record UserToken(String tokenValue, Instant expiresAt, String subject) {

    /**
     * Returns {@code true} when the token has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
