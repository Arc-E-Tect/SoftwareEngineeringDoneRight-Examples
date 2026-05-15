package com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.UserToken;

/**
 * Secondary port – interaction with the Authorization Service.
 *
 * <p>The Authorization Service accepts the user token (from the OIDC Provider) and returns an inner token
 * enriched with RBAC and ABAC claims.  The inner token is attached to every
 * outbound request to the microservice.
 */
public interface InnerTokenServicePort {

    /**
     * Exchange a user token for an inner token.
     *
     * @param userToken the OIDC user token for the current session
     * @return an inner token carrying the authorization claims
     */
    InnerToken swapForInnerToken(UserToken userToken);
}
