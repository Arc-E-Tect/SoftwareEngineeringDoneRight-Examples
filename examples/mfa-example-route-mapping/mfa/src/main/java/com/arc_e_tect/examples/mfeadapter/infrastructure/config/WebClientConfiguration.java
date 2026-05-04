package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * {@link WebClient} beans for outbound HTTP calls from the MFA.
 *
 * <p>Two clients are configured:
 * <ol>
 *   <li><b>microserviceWebClient</b> – mTLS-enabled client used to forward
 *       requests to the associated microservice.</li>
 *   <li><b>authorizationServiceWebClient</b> – mTLS-enabled client used to call
 *       the Authorization Service for inner-token exchange.</li>
 *   <li><b>oidcProviderWebClient</b> – plain TLS client used to talk to the OIDC Provider
 *       (the OIDC Provider typically terminates at the load balancer).</li>
 * </ol>
 */
@org.springframework.context.annotation.Profile("!payload-shaper && !route-mapping")
@Configuration
public class WebClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfiguration.class);

    private final MfeAdapterProperties mfeAdapterProperties;

    public WebClientConfiguration(MfeAdapterProperties mfeAdapterProperties) {
        this.mfeAdapterProperties = mfeAdapterProperties;
    }

    @Bean("microserviceWebClient")
    public WebClient microserviceWebClient() {
        HttpClient httpClient = buildMtlsHttpClient();
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getMicroservice().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("authorizationServiceWebClient")
    public WebClient authorizationServiceWebClient() {
        HttpClient httpClient = buildMtlsHttpClient();
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getAuthorizationService().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean("oidcProviderWebClient")
    public WebClient oidcProviderWebClient() {
        return WebClient.builder()
                .baseUrl(mfeAdapterProperties.getOidcProvider().getIssuerUri())
                .build();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private HttpClient buildMtlsHttpClient() {
        MfeAdapterProperties.Mtls mtls = mfeAdapterProperties.getMtls();
        if (!mtls.isEnabled()) {
            log.warn("mTLS is DISABLED – outbound connections will use plain TLS");
            return HttpClient.create();
        }
        try {
            KeyManagerFactory kmf = loadKeyManagerFactory(mtls);
            TrustManagerFactory tmf = loadTrustManagerFactory(mtls);
            SslContext sslContext = SslContextBuilder.forClient()
                    .keyManager(kmf)
                    .trustManager(tmf)
                    .build();
            return HttpClient.create()
                    .secure(spec -> spec.sslContext(sslContext));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure mTLS for outbound WebClient", e);
        }
    }

    private KeyManagerFactory loadKeyManagerFactory(MfeAdapterProperties.Mtls mtls) throws Exception {
        KeyStore ks = KeyStore.getInstance(mtls.getKeyStoreType());
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(stripClasspath(mtls.getKeyStore()))) {
            ks.load(is, mtls.getKeyStorePassword().toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, mtls.getKeyStorePassword().toCharArray());
        return kmf;
    }

    private TrustManagerFactory loadTrustManagerFactory(MfeAdapterProperties.Mtls mtls) throws Exception {
        KeyStore ts = KeyStore.getInstance(mtls.getTrustStoreType());
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(stripClasspath(mtls.getTrustStore()))) {
            ts.load(is, mtls.getTrustStorePassword().toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf;
    }

    private String stripClasspath(String location) {
        return location != null && location.startsWith("classpath:")
                ? location.substring("classpath:".length())
                : location;
    }
}
