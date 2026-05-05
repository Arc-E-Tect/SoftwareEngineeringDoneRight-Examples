package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Supplements existing coverage for MfeAdapterProperties inner-class
 * setters and getters that are not exercised by other tests.
 */
@DisplayName("MfeAdapterProperties – supplemental coverage")
class MfeAdapterPropertiesSupplementTest {

    // -----------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Session – required flag defaults to true and can be set")
    void session_requiredFlag() {
        MfeAdapterProperties.Session session = new MfeAdapterProperties.Session();
        assertThat(session.isRequired()).isTrue();
        session.setRequired(false);
        assertThat(session.isRequired()).isFalse();
    }

    // -----------------------------------------------------------------
    // OidcProvider
    // -----------------------------------------------------------------

    @Test
    @DisplayName("OidcProvider – setters and getters round-trip")
    void oidcProvider_settersGetters() {
        MfeAdapterProperties.OidcProvider oidc = new MfeAdapterProperties.OidcProvider();
        oidc.setIssuerUri("http://idp.example.com");
        oidc.setRealm("mfa-realm");
        oidc.setClientId("client-id");
        oidc.setClientSecret("secret");

        assertThat(oidc.getIssuerUri()).isEqualTo("http://idp.example.com");
        assertThat(oidc.getRealm()).isEqualTo("mfa-realm");
        assertThat(oidc.getClientId()).isEqualTo("client-id");
        assertThat(oidc.getClientSecret()).isEqualTo("secret");
    }

    // -----------------------------------------------------------------
    // AuthorizationService
    // -----------------------------------------------------------------

    @Test
    @DisplayName("AuthorizationService – required flag can be set to false")
    void authorizationService_requiredFlag() {
        MfeAdapterProperties.AuthorizationService auth = new MfeAdapterProperties.AuthorizationService();
        assertThat(auth.isRequired()).isTrue();
        auth.setRequired(false);
        auth.setBaseUrl("http://secservice.example.com");
        auth.setTokenSwapPath("/swap");

        assertThat(auth.isRequired()).isFalse();
        assertThat(auth.getBaseUrl()).isEqualTo("http://secservice.example.com");
        assertThat(auth.getTokenSwapPath()).isEqualTo("/swap");
    }

    // -----------------------------------------------------------------
    // Microservice
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Microservice – setters and getters round-trip")
    void microservice_settersGetters() {
        MfeAdapterProperties.Microservice ms = new MfeAdapterProperties.Microservice();
        ms.setBaseUrl("http://localhost:8081");
        ms.setContextPath("/v1");

        assertThat(ms.getBaseUrl()).isEqualTo("http://localhost:8081");
        assertThat(ms.getContextPath()).isEqualTo("/v1");
    }

    // -----------------------------------------------------------------
    // Versioning
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Versioning – versionHeader can be overridden")
    void versioning_versionHeader() {
        MfeAdapterProperties.Versioning v = new MfeAdapterProperties.Versioning();
        assertThat(v.getVersionHeader()).isEqualTo("X-API-Version");
        v.setVersionHeader("X-Version");
        assertThat(v.getVersionHeader()).isEqualTo("X-Version");
    }

    // -----------------------------------------------------------------
    // Routing
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Routing – stripPrefix and pathMappings can be set")
    void routing_settersGetters() {
        MfeAdapterProperties.Routing r = new MfeAdapterProperties.Routing();
        assertThat(r.isStripPrefix()).isFalse();
        r.setStripPrefix(true);
        r.setPathMappings(Map.of("/api/v1", "/v1"));

        assertThat(r.isStripPrefix()).isTrue();
        assertThat(r.getPathMappings()).containsKey("/api/v1");
    }

    // -----------------------------------------------------------------
    // Mtls
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Mtls – all fields can be set and retrieved")
    void mtls_settersGetters() {
        MfeAdapterProperties.Mtls mtls = new MfeAdapterProperties.Mtls();
        mtls.setEnabled(true);
        mtls.setKeyStore("classpath:ssl/ks.p12");
        mtls.setKeyStorePassword("ks-pass");
        mtls.setKeyStoreType("JKS");
        mtls.setTrustStore("classpath:ssl/ts.p12");
        mtls.setTrustStorePassword("ts-pass");
        mtls.setTrustStoreType("JKS");

        assertThat(mtls.isEnabled()).isTrue();
        assertThat(mtls.getKeyStore()).isEqualTo("classpath:ssl/ks.p12");
        assertThat(mtls.getKeyStorePassword()).isEqualTo("ks-pass");
        assertThat(mtls.getKeyStoreType()).isEqualTo("JKS");
        assertThat(mtls.getTrustStore()).isEqualTo("classpath:ssl/ts.p12");
        assertThat(mtls.getTrustStorePassword()).isEqualTo("ts-pass");
        assertThat(mtls.getTrustStoreType()).isEqualTo("JKS");
    }

    // -----------------------------------------------------------------
    // Kafka inner classes (properties only; beans excluded in vanilla)
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Kafka – group id and topic pattern can be set")
    void kafka_settersGetters() {
        MfeAdapterProperties.Kafka kafka = new MfeAdapterProperties.Kafka();
        kafka.getConsumer().setGroupId("my-group");
        kafka.getReferenceData().setTopicPattern("my-topic\\..*");
        MfeAdapterProperties.Kafka.Consumer consumer = new MfeAdapterProperties.Kafka.Consumer();
        kafka.setConsumer(consumer);
        MfeAdapterProperties.Kafka.ReferenceData rd = new MfeAdapterProperties.Kafka.ReferenceData();
        kafka.setReferenceData(rd);

        assertThat(kafka.getConsumer().getGroupId()).isEqualTo("mfa-reference-data");
        assertThat(kafka.getReferenceData().getTopicPattern()).isEqualTo("reference-data\\..*");
    }

    // -----------------------------------------------------------------
    // Root MfeAdapterProperties wiring
    // -----------------------------------------------------------------

    @Test
    @DisplayName("Root properties – all nested objects can be replaced via setters")
    void root_allSettersGetters() {
        MfeAdapterProperties p = new MfeAdapterProperties();
        p.setSession(new MfeAdapterProperties.Session());
        p.setApiGateway(new MfeAdapterProperties.ApiGateway());
        p.setOidcProvider(new MfeAdapterProperties.OidcProvider());
        p.setAuthorizationService(new MfeAdapterProperties.AuthorizationService());
        p.setMicroservice(new MfeAdapterProperties.Microservice());
        p.setVersioning(new MfeAdapterProperties.Versioning());
        p.setRouting(new MfeAdapterProperties.Routing());
        p.setMtls(new MfeAdapterProperties.Mtls());
        p.setKafka(new MfeAdapterProperties.Kafka());

        assertThat(p.getSession()).isNotNull();
        assertThat(p.getApiGateway()).isNotNull();
        assertThat(p.getOidcProvider()).isNotNull();
        assertThat(p.getAuthorizationService()).isNotNull();
        assertThat(p.getMicroservice()).isNotNull();
        assertThat(p.getVersioning()).isNotNull();
        assertThat(p.getRouting()).isNotNull();
        assertThat(p.getMtls()).isNotNull();
        assertThat(p.getKafka()).isNotNull();
    }
}
