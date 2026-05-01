package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.IdentityProviderPort;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config.MfeAdapterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

/**
 * Adapter that communicates with the OIDC Provider for the OAuth2 Authorization
 * Code flow.
 *
 * <p>Token endpoint, revocation endpoint, and authorization URL construction
 * are all delegated to the OIDC Provider's standard endpoints.
 */
@Component
public class OidcProviderAdapter implements IdentityProviderPort {

    private static final Logger log = LoggerFactory.getLogger(OidcProviderAdapter.class);

    private final WebClient webClient;
    private final MfeAdapterProperties mfeAdapterProperties;

    public OidcProviderAdapter(@Qualifier("oidcProviderWebClient") WebClient webClient,
                            MfeAdapterProperties mfeAdapterProperties) {
        this.webClient = webClient;
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Override
    public UserToken exchangeCodeForToken(String authorizationCode, String redirectUri) {
        MfeAdapterProperties.OidcProvider kc = mfeAdapterProperties.getOidcProvider();
        String tokenEndpoint = kc.getIssuerUri() + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", authorizationCode);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", kc.getClientId());
        formData.add("client_secret", kc.getClientSecret());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
                .uri(tokenEndpoint)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Invalid token response from OIDC provider");
        }

        String accessToken = (String) response.get("access_token");
        Number expiresIn = (Number) response.getOrDefault("expires_in", 300);
        String subject = extractSubjectFromJwt(accessToken);

        return new UserToken(
                accessToken,
                Instant.now().plusSeconds(expiresIn.longValue()),
                subject);
    }

    @Override
    public void revokeToken(UserToken userToken) {
        MfeAdapterProperties.OidcProvider kc = mfeAdapterProperties.getOidcProvider();
        String revocationEndpoint = kc.getIssuerUri() + "/protocol/openid-connect/revoke";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", userToken.tokenValue());
        formData.add("client_id", kc.getClientId());
        formData.add("client_secret", kc.getClientSecret());

        webClient.post()
                .uri(revocationEndpoint)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toBodilessEntity()
                .block();

        log.debug("Token revoked for subject '{}'", userToken.subject());
    }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        MfeAdapterProperties.OidcProvider kc = mfeAdapterProperties.getOidcProvider();
        String authEndpoint = kc.getIssuerUri() + "/protocol/openid-connect/auth";

        return UriComponentsBuilder.fromUriString(authEndpoint)
                .queryParam("response_type", "code")
                .queryParam("client_id", kc.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile email")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /**
     * Extract the {@code sub} claim from a JWT without full signature
     * verification (the token is already trusted at this point – it was
     * received directly from the OIDC provider over TLS).
     */
    private String extractSubjectFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return "unknown";
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Simple extraction – in production use a proper JWT library
            int subStart = payload.indexOf("\"sub\":\"");
            if (subStart < 0) return "unknown";
            int valueStart = subStart + 7;
            int valueEnd = payload.indexOf("\"", valueStart);
            return payload.substring(valueStart, valueEnd);
        } catch (Exception e) {
            log.warn("Could not extract subject from JWT: {}", e.getMessage());
            return "unknown";
        }
    }
}
