package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter;

import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Standard {@link ApiKeyValidator} that enforces the Zero-Trust API-key
 * contract between an API Gateway (AG) and the MFA.
 *
 * <p>The AG validates the API key sent by every MFE request and — when valid —
 * strips the raw key and adds a configurable header asserting successful
 * validation.  This validator reads that header and rejects any request for
 * which the AG did not confirm a valid API key.
 *
 * <p>When the validation succeeds, the granted {@link ApiKeyScope} is read
 * from the configurable scope header (default: {@code X-API-Key-Scope}) and
 * returned.  If no scope header is present, {@link ApiKeyScope#READ} is
 * assumed as the safe default.
 *
 * <p>Enable this validator by declaring it as a Spring bean, for example in
 * your application {@code @Configuration} class:
 * <pre>{@code
 * @Bean
 * public ApiKeyValidator apiKeyValidator(MfeAdapterProperties properties) {
 *     return new StandardApiKeyValidator(properties);
 * }
 * }</pre>
 *
 * <p>The header names and expected value are controlled by:
 * <pre>{@code
 * mfe-adapter:
 *   api-gateway:
 *     validated-header: X-API-Key-Validated   # header set by the AG
 *     validated-value:  "true"                 # expected value
 *     scope-header:     X-API-Key-Scope        # scope header set by the AG
 * }</pre>
 *
 * @see ApiKeyValidator
 * @see MfeAdapterProperties.ApiGateway
 */
public class StandardApiKeyValidator implements ApiKeyValidator {

    private static final Logger log = LoggerFactory.getLogger(StandardApiKeyValidator.class);

    private final MfeAdapterProperties mfeAdapterProperties;

    public StandardApiKeyValidator(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    /**
     * Returns the granted {@link ApiKeyScope} when the AG has confirmed
     * API-key validity by including the expected header with the expected
     * value.  Returns {@link Optional#empty()} to reject the request.
     */
    @Override
    public Optional<ApiKeyScope> validate(HttpServletRequest request) {
        MfeAdapterProperties.ApiGateway ag = mfeAdapterProperties.getApiGateway();
        String headerValue = request.getHeader(ag.getValidatedHeader());
        boolean valid = ag.getValidatedValue().equalsIgnoreCase(headerValue);
        if (!valid) {
            log.warn("Rejected request to '{}': missing or invalid API-key validation header",
                    request.getRequestURI());
            return Optional.empty();
        }

        String scopeValue = request.getHeader(ag.getScopeHeader());
        ApiKeyScope scope = ApiKeyScope.fromHeaderValue(scopeValue);
        if (scope == null) {
            // AG validated the key but sent no recognisable scope → default to READ
            log.debug("No scope header '{}' on request to '{}'; defaulting to READ",
                    ag.getScopeHeader(), request.getRequestURI());
            scope = ApiKeyScope.READ;
        }
        return Optional.of(scope);
    }
}
