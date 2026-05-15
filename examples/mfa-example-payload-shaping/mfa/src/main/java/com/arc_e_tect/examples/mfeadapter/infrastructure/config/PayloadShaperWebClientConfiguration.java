package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Payload-shaper (no-auth, no-mTLS) replacement for {@link WebClientConfiguration}.
 *
 * <p>Provides a plain HTTP {@link WebClient} bean named {@code microserviceWebClient}
 * suitable for the payload-shaper example where no TLS or OIDC client credentials are needed.
 */
@Profile("payload-shaper")
@Configuration
public class PayloadShaperWebClientConfiguration {

    private final MfeAdapterProperties mfeAdapterProperties;

    public PayloadShaperWebClientConfiguration(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Bean("microserviceWebClient")
    public WebClient microserviceWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getMicroservice().getBaseUrl())
                .build();
    }
}
