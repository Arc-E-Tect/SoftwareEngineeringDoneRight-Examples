package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityMfaWebClientConfiguration")
class SecurityMfaWebClientConfigurationTest {

    private MfeAdapterProperties propsWithUrls() {
        MfeAdapterProperties props = new MfeAdapterProperties();
        props.getMicroservice().setBaseUrl("http://ms:8081");
        props.getOidcProvider().setIssuerUri("http://wiremock-idp:8080/realms/mfa");
        props.getAuthorizationService().setBaseUrl("http://wiremock-authz:8080");
        return props;
    }

    @Test
    @DisplayName("microserviceWebClient is created from MfeAdapterProperties microservice base-url")
    void microserviceWebClient_usesBaseUrl() {
        SecurityMfaWebClientConfiguration config = new SecurityMfaWebClientConfiguration(propsWithUrls());
        WebClient client = config.microserviceWebClient();
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("oidcProviderWebClient is created from MfeAdapterProperties oidc issuer-uri")
    void oidcProviderWebClient_usesIssuerUri() {
        SecurityMfaWebClientConfiguration config = new SecurityMfaWebClientConfiguration(propsWithUrls());
        WebClient client = config.oidcProviderWebClient();
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("authorizationServiceWebClient is created from MfeAdapterProperties authorization-service base-url")
    void authorizationServiceWebClient_usesBaseUrl() {
        SecurityMfaWebClientConfiguration config = new SecurityMfaWebClientConfiguration(propsWithUrls());
        WebClient client = config.authorizationServiceWebClient();
        assertThat(client).isNotNull();
    }
}
