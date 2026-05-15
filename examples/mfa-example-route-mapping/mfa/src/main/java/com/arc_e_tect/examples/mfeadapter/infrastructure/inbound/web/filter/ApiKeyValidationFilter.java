package com.arc_e_tect.examples.mfeadapter.infrastructure.inbound.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet filter that delegates API-key validation to an injected
 * {@link ApiKeyValidator}.
 *
 * <p>If no {@code ApiKeyValidator} bean is present in the application context,
 * all requests pass through without inspection.  Declare a validator bean to
 * activate enforcement:
 * <pre>{@code
 * @Bean
 * public ApiKeyValidator apiKeyValidator(MfeAdapterProperties properties) {
 *     return new StandardApiKeyValidator(properties);
 * }
 * }</pre>
 *
 * <p>When validation succeeds the resolved {@link ApiKeyScope} is stored as a
 * request attribute under the key {@value #SCOPE_ATTRIBUTE} so downstream
 * components can make scope-aware decisions.
 *
 * <p>Actuator health and info endpoints are always exempt from validation,
 * regardless of which validator is configured.
 *
 * @see ApiKeyValidator
 * @see StandardApiKeyValidator
 */
@Component
@Order(1)
public class ApiKeyValidationFilter extends OncePerRequestFilter {

    /** Request attribute key under which the granted {@link ApiKeyScope} is stored. */
    public static final String SCOPE_ATTRIBUTE = "mfa.api-key-scope";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyValidationFilter.class);

    private final ApiKeyValidator validator;

    /**
     * @param validator optional API-key validator; if absent the filter is a no-op
     */
    public ApiKeyValidationFilter(Optional<ApiKeyValidator> validator) {
        this.validator = validator.orElse(null);
        if (this.validator == null) {
            log.info("No ApiKeyValidator bean configured – API-key validation is disabled");
        } else {
            log.info("API-key validation enabled via {}", this.validator.getClass().getSimpleName());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (validator != null) {
            Optional<ApiKeyScope> scopeOpt = validator.validate(request);
            if (scopeOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"error\":\"forbidden\",\"message\":\"API key validation required\"}");
                return;
            }
            request.setAttribute(SCOPE_ATTRIBUTE, scopeOpt.get());
        }

        filterChain.doFilter(request, response);
    }
}
