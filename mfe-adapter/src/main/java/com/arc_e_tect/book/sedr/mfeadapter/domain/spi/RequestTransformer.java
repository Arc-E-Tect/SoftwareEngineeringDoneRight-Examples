package com.arc_e_tect.book.sedr.mfeadapter.domain.spi;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ProxiedRequest;

/**
 * SPI – transforms a {@link ProxiedRequest} before it is forwarded to the
 * associated microservice.
 *
 * <p>Implement this interface and register the implementation as a Spring
 * bean to apply MFA-specific transformations such as:
 * <ul>
 *   <li>renaming request fields to align with the MS contract;</li>
 *   <li>injecting additional headers required by the MS;</li>
 *   <li>stripping headers that must not reach the MS.</li>
 * </ul>
 *
 * <p>Multiple transformers may coexist; they are applied in {@code @Order}
 * sequence.  A no-op implementation is provided by the framework when no
 * bean is found.
 */
public interface RequestTransformer {

    /**
     * Transform the given request.
     *
     * @param request the original (validated) request
     * @return the transformed request; never {@code null}
     */
    ProxiedRequest transform(ProxiedRequest request);

    /**
     * Guards whether this transformer applies to the given request.
     * Return {@code false} to skip this transformer for the request.
     *
     * @param request the request to test
     * @return {@code true} when this transformer should be applied
     */
    default boolean supports(ProxiedRequest request) {
        return true;
    }
}
