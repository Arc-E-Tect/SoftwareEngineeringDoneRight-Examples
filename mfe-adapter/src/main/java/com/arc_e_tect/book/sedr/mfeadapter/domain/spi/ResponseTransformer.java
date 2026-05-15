package com.arc_e_tect.book.sedr.mfeadapter.domain.spi;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedResponse;

/**
 * SPI – transforms a {@link ProxiedResponse} before it is returned to the MFE.
 *
 * <p>Implement this interface and register the implementation as a Spring
 * bean to apply MFA-specific response shaping, for example:
 * <ul>
 *   <li>renaming response fields to align with the MFE contract;</li>
 *   <li>filtering or enriching response data;</li>
 *   <li>adding or removing response headers.</li>
 * </ul>
 *
 * <p>Multiple transformers may coexist; they are applied in {@code @Order}
 * sequence.  A no-op implementation is provided by the framework when no
 * bean is found.
 */
public interface ResponseTransformer {

    /**
     * Transform the microservice response.
     *
     * @param response        the raw response received from the microservice
     * @param originalRequest the request that produced this response
     * @return the transformed response; never {@code null}
     */
    ProxiedResponse transform(ProxiedResponse response, ProxiedRequest originalRequest);

    /**
     * Guards whether this transformer applies to the given response.
     * Return {@code false} to skip this transformer.
     *
     * @param response the response to test
     * @return {@code true} when this transformer should be applied
     */
    default boolean supports(ProxiedResponse response) {
        return true;
    }
}
