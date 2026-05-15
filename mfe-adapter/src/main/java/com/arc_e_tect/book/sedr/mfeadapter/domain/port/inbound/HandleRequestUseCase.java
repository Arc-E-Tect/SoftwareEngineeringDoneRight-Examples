package com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;

/**
 * Primary port – handles a proxied HTTP request.
 *
 * <p>This is the central use case of the MFA.  The implementation
 * orchestrates authentication, inner-token acquisition, optional
 * request/response transformation, and the actual forwarding of the
 * request to the microservice.
 */
public interface HandleRequestUseCase {

    /**
     * Process the incoming request and return the response to be sent
     * back to the MFE.
     *
     * @param request the enriched, domain-level representation of the
     *                inbound HTTP request
     * @return the response to be forwarded to the MFE
     */
    ProxiedResponse handle(ProxiedRequest request);
}
