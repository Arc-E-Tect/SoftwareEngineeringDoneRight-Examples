package com.arc_e_tect.examples.mfeadapter.domain.model;

import java.time.Instant;

/**
 * Domain model representing an authenticated session.
 *
 * <p>A session is created by the MFA when a user successfully authenticates
 * through the OIDC Provider.  Its identifier is stored in a cookie and sent to the
 * MFE.  The corresponding {@link UserToken} is kept server-side.
 *
 * @param sessionId   opaque identifier stored in the session cookie
 * @param userToken   the OIDC user token bound to this session
 * @param expiresAt   wall-clock instant after which the session is no longer valid
 * @param subject     the identity of the authenticated user (OIDC subject claim)
 */
public record Session(String sessionId, UserToken userToken, Instant expiresAt, String subject) {

    /**
     * Returns {@code true} when the session has passed its expiry instant.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
