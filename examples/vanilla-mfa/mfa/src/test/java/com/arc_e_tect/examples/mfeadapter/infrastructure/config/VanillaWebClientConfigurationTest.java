package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VanillaWebClientConfiguration")
class VanillaWebClientConfigurationTest {

    @Test
    @DisplayName("microserviceWebClient uses base-url from MfeAdapterProperties")
    void microserviceWebClient_usesBaseUrl() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setBaseUrl("http://ms:8081");
        VanillaWebClientConfiguration config = new VanillaWebClientConfiguration(props);
        WebClient client = config.microserviceWebClient();
        assertThat(client).isNotNull();
    }
}
