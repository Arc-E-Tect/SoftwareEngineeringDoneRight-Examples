package com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;

/**
 * Secondary port – outbound HTTP client for the associated microservice.
 *
 * <p>The adapter implementation uses a mTLS-enabled {@code WebClient} to
 * forward requests and carries the inner token as a Bearer token in the
 * {@code Authorization} header.
 */
public interface MicroserviceClientPort {

    /**
     * Forward the request to the microservice and return its response.
     *
     * @param request    the (optionally transformed) request to forward
     * @param innerToken the inner token issued by SecService, used as Bearer credential
     * @return the raw response from the microservice
     */
    ProxiedResponse forward(ProxiedRequest request, InnerToken innerToken);
}
