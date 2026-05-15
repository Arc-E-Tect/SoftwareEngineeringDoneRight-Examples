package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Vanilla (no-auth, no-mTLS) replacement for {@link WebClientConfiguration}.
 *
 * <p>Provides a plain HTTP {@link WebClient} bean named {@code microserviceWebClient}
 * suitable for the vanilla example where no TLS or OIDC client credentials are needed.
 */
@Profile("vanilla")
@Configuration
public class VanillaWebClientConfiguration {

    private final MfeAdapterProperties mfeAdapterProperties;

    public VanillaWebClientConfiguration(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Bean("microserviceWebClient")
    public WebClient microserviceWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getMicroservice().getBaseUrl())
                .build();
    }
}
