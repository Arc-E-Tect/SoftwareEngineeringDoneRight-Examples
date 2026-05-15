package com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.Session;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.UserToken;

/**
 * Primary port – handles the OAuth2 Authorization Code callback from the OIDC Provider.
 *
 * <p>After the user authenticates at the OIDC Provider, the OIDC Provider redirects to the MFA
 * callback URL with an authorization code.  This use case exchanges the code
 * for a user token, stores the token in the session store, and returns a new
 * {@link Session} whose identifier is embedded in the session cookie.
 */
public interface AuthenticateUserUseCase {

    /**
     * Exchange an OAuth2 authorization code for a user token and create a
     * new session.
     *
     * @param authorizationCode the code received from the OIDC Provider
     * @param redirectUri       the redirect URI that was originally sent to the OIDC Provider
     * @return a freshly created session
     */
    Session authenticate(String authorizationCode, String redirectUri);

    /**
     * Invalidate an existing session (logout).
     *
     * @param sessionId the session identifier taken from the session cookie
     */
    void logout(String sessionId);

    /**
     * Retrieve and validate an existing session by its cookie value.
     *
     * @param sessionId the session identifier taken from the session cookie
     * @return the valid session
     * @throws com.arc_e_tect.book.sedr.mfeadapter.application.exception.AuthenticationException when
     *         the session does not exist or has expired
     */
    Session getValidSession(String sessionId);
}
