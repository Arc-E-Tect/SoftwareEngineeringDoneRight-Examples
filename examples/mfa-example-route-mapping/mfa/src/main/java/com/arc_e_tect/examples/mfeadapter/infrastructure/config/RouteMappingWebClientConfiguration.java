package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Route-mapping (no-auth, no-mTLS) replacement for {@link WebClientConfiguration}.
 *
 * <p>Provides a plain HTTP {@link WebClient} bean named {@code microserviceWebClient}
 * suitable for the route-mapping example where no TLS or OIDC client credentials are needed.
 */
@Profile("route-mapping")
@Configuration
public class RouteMappingWebClientConfiguration {

    private final MfeAdapterProperties mfeAdapterProperties;

    public RouteMappingWebClientConfiguration(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Bean("microserviceWebClient")
    public WebClient microserviceWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getMicroservice().getBaseUrl())
                .build();
    }
}
