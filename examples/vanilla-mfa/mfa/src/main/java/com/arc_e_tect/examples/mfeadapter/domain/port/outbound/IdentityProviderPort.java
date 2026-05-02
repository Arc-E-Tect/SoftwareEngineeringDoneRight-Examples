package com.arc_e_tect.examples.mfeadapter.domain.port.outbound;

import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;

/**
 * Secondary port – interaction with the identity provider (OIDC Provider).
 *
 * <p>The MFA uses the OAuth2 Authorization Code flow.  This port
 * encapsulates the token endpoint call that exchanges the authorization
 * code for a user token.
 */
public interface IdentityProviderPort {

    /**
     * Exchange an OAuth2 authorization code for a user token.
     *
     * @param authorizationCode the code returned by the OIDC Provider to the callback URL
     * @param redirectUri       the redirect URI originally sent in the authorization request
     * @return the user token returned by the token endpoint
     */
    UserToken exchangeCodeForToken(String authorizationCode, String redirectUri);

    /**
     * Initiate a logout at the identity provider, revoking the given token.
     *
     * @param userToken the token to revoke
     */
    void revokeToken(UserToken userToken);

    /**
     * Build the authorization URL to which the MFA should redirect the
     * user-agent for login.
     *
     * @param redirectUri the URI the OIDC Provider should redirect to after authentication
     * @param state       an opaque CSRF state value
     * @return the full authorization URL
     */
    String buildAuthorizationUrl(String redirectUri, String state);
}
