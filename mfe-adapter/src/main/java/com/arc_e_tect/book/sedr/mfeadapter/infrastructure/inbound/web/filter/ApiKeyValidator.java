package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * SPI interface for API-key validation.
 *
 * <p>Declare an implementation as a Spring {@code @Bean} to activate API-key
 * validation for all inbound requests.  If no bean is present in the
 * application context, the {@link ApiKeyValidationFilter} passes every request
 * through without inspection.
 *
 * <p>The {@link StandardApiKeyValidator} is a ready-made implementation that
 * checks for the validation header set by an API Gateway.  Enable it with a
 * single bean declaration:
 * <pre>{@code
 * @Bean
 * public ApiKeyValidator apiKeyValidator(MfeAdapterProperties properties) {
 *     return new StandardApiKeyValidator(properties);
 * }
 * }</pre>
 *
 * @see StandardApiKeyValidator
 * @see ApiKeyValidationFilter
 */
public interface ApiKeyValidator {

    /**
     * Validates the request and, if valid, returns the {@link ApiKeyScope}
     * granted by the API Gateway.
     *
     * <p>Return {@link Optional#empty()} to reject the request with
     * {@code 403 Forbidden}.  Return a non-empty {@code Optional} to allow
     * the request and carry the resolved scope downstream.
     *
     * @param request the inbound HTTP request
     * @return the granted scope, or empty to reject
     */
    Optional<ApiKeyScope> validate(HttpServletRequest request);
}
