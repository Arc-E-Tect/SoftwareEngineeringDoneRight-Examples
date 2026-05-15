package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Security-MFA WebClient configuration.
 *
 * <p>Provides plain HTTP (no mTLS) {@link WebClient} beans for all three
 * outbound targets used in the security-mfa example:
 * <ol>
 *   <li>{@code microserviceWebClient} – calls the backend microservice</li>
 *   <li>{@code oidcProviderWebClient} – calls the WireMock OIDC IdP</li>
 *   <li>{@code authorizationServiceWebClient} – calls the WireMock Authorization Service</li>
 * </ol>
 *
 * <p>mTLS is intentionally disabled in this example to keep the topology
 * simple and focus attention on the session-based security pattern.
 */
@Profile("security-mfa")
@Configuration
public class SecurityMfaWebClientConfiguration {

    private final MfeAdapterProperties mfeAdapterProperties;

    public SecurityMfaWebClientConfiguration(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Bean("microserviceWebClient")
    public WebClient microserviceWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getMicroservice().getBaseUrl())
                .build();
    }

    @Bean("oidcProviderWebClient")
    public WebClient oidcProviderWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getOidcProvider().getIssuerUri())
                .build();
    }

    @Bean("authorizationServiceWebClient")
    public WebClient authorizationServiceWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getAuthorizationService().getBaseUrl())
                .build();
    }
}
