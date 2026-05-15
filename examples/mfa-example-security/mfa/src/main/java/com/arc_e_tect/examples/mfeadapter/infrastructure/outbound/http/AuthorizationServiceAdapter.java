package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.InnerToken;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.InnerTokenServicePort;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Adapter that calls the Authorization Service to swap the
 * OIDC user token for an inner token enriched with RBAC and ABAC claims.
 *
 * <p>Communication with the Authorization Service uses the same mTLS {@link WebClient} configured
 * for microservice calls.
 */
@ConditionalOnProperty(name = "mfe-adapter.authorization-service.required", havingValue = "true")
@Component
public class AuthorizationServiceAdapter implements InnerTokenServicePort {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServiceAdapter.class);

    private final WebClient webClient;
    private final MfeAdapterProperties mfeAdapterProperties;

    public AuthorizationServiceAdapter(@Qualifier("authorizationServiceWebClient") WebClient webClient,
                       MfeAdapterProperties mfeAdapterProperties) {
        this.webClient = webClient;
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public InnerToken swapForInnerToken(UserToken userToken) {
        log.debug("Swapping user token for inner token at Authorization Service");

        String swapPath = mfeAdapterProperties.getAuthorizationService().getTokenSwapPath();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
                .uri(swapPath)
                .header("Authorization", "Bearer " + userToken.tokenValue())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("token")) {
            throw new IllegalStateException("Invalid inner-token response from Authorization Service");
        }

        String innerTokenValue = (String) response.get("token");
        Number expiresIn = (Number) response.getOrDefault("expires_in", 300);

        @SuppressWarnings("unchecked")
        Map<String, Object> claims = (Map<String, Object>) response.getOrDefault("claims", Map.of());

        return new InnerToken(
                innerTokenValue,
                Map.copyOf(claims),
                Instant.now().plusSeconds(expiresIn.longValue()));
    }
}
