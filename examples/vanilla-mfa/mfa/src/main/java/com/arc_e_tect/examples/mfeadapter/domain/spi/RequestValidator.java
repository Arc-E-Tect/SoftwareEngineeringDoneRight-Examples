package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.domain.model.ValidationResult;

/**
 * SPI – validates a {@link ProxiedRequest} before it is forwarded to the
 * associated microservice.
 *
 * <p>Implement this interface and register the implementation as a Spring
 * bean to add MFA-specific validation logic, for example:
 * <ul>
 *   <li>checking that a country code in the request body exists in the
 *       reference-data cache;</li>
 *   <li>enforcing business rules that are MFA-specific and not duplicated
 *       in the microservice.</li>
 * </ul>
 *
 * <p>Multiple validators may coexist; all applicable validators are executed
 * and their results are aggregated before any forwarding takes place.
 * The framework provides a permissive no-op validator when no bean is found.
 */
public interface RequestValidator {

    /**
     * Validate the request.
     *
     * @param request the request to validate
     * @return a {@link ValidationResult} describing whether the request is valid
     */
    ValidationResult validate(ProxiedRequest request);

    /**
     * Guards whether this validator applies to the given request.
     *
     * @param request the request to test
     * @return {@code true} when this validator should run
     */
    default boolean supports(ProxiedRequest request) {
        return true;
    }
}
