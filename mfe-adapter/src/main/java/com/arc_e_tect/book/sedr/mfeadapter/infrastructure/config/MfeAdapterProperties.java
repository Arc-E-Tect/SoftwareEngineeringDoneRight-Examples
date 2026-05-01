package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Strongly-typed configuration properties for the MFA template.
 *
 * <p>All values are bound from the {@code mfe-adapter.*} namespace in
 * {@code application.yml} / environment variables.  Each MFA instance
 * overrides the relevant properties for its specific deployment.
 */
@ConfigurationProperties(prefix = "mfe-adapter")
public class MfeAdapterProperties {

    private Session session = new Session();
    private ApiGateway apiGateway = new ApiGateway();
    private OidcProvider oidcProvider = new OidcProvider();
    private AuthorizationService authorizationService = new AuthorizationService();
    private Microservice microservice = new Microservice();
    private Versioning versioning = new Versioning();
    private Routing routing = new Routing();
    private Mtls mtls = new Mtls();
    private Kafka kafka = new Kafka();

    // -----------------------------------------------------------------
    // Getters / setters
    // -----------------------------------------------------------------

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public ApiGateway getApiGateway() { return apiGateway; }
    public void setApiGateway(ApiGateway apiGateway) { this.apiGateway = apiGateway; }

    public OidcProvider getOidcProvider() { return oidcProvider; }
    public void setOidcProvider(OidcProvider oidcProvider) { this.oidcProvider = oidcProvider; }

    public AuthorizationService getAuthorizationService() { return authorizationService; }
    public void setAuthorizationService(AuthorizationService authorizationService) { this.authorizationService = authorizationService; }

    public Microservice getMicroservice() { return microservice; }
    public void setMicroservice(Microservice microservice) { this.microservice = microservice; }

    public Versioning getVersioning() { return versioning; }
    public void setVersioning(Versioning versioning) { this.versioning = versioning; }

    public Routing getRouting() { return routing; }
    public void setRouting(Routing routing) { this.routing = routing; }

    public Mtls getMtls() { return mtls; }
    public void setMtls(Mtls mtls) { this.mtls = mtls; }

    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }

    // -----------------------------------------------------------------
    // Nested configuration classes
    // -----------------------------------------------------------------

    public static class Session {
        /** Name of the cookie sent to the MFE. */
        private String cookieName = "MFESESSION";
        /** Session lifetime in minutes. */
        private long ttlMinutes = 30;
        /** Mark the session cookie as HTTP-only. */
        private boolean httpOnly = true;
        /** Mark the session cookie as Secure (HTTPS only). */
        private boolean secure = true;
        /** SameSite attribute value for the session cookie. */
        private String sameSite = "Strict";

        public String getCookieName() { return cookieName; }
        public void setCookieName(String cookieName) { this.cookieName = cookieName; }
        public long getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(long ttlMinutes) { this.ttlMinutes = ttlMinutes; }
        public boolean isHttpOnly() { return httpOnly; }
        public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
    }

    public static class ApiGateway {
        /** Header name added by the API Gateway after successful API-key validation. */
        private String validatedHeader = "X-API-Key-Validated";
        /** Expected value of the validated header. */
        private String validatedValue = "true";
        /** Header name carrying the granted API-key scope (e.g. {@code X-API-Key-Scope}). */
        private String scopeHeader = "X-API-Key-Scope";
        /** Whether to forward the resolved scope header to the downstream microservice. */
        private boolean forwardScope = true;

        public String getValidatedHeader() { return validatedHeader; }
        public void setValidatedHeader(String validatedHeader) { this.validatedHeader = validatedHeader; }
        public String getValidatedValue() { return validatedValue; }
        public void setValidatedValue(String validatedValue) { this.validatedValue = validatedValue; }
        public String getScopeHeader() { return scopeHeader; }
        public void setScopeHeader(String scopeHeader) { this.scopeHeader = scopeHeader; }
        public boolean isForwardScope() { return forwardScope; }
        public void setForwardScope(boolean forwardScope) { this.forwardScope = forwardScope; }
    }

    public static class OidcProvider {
        private String issuerUri;
        private String realm;
        private String clientId;
        private String clientSecret;

        public String getIssuerUri() { return issuerUri; }
        public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
        public String getRealm() { return realm; }
        public void setRealm(String realm) { this.realm = realm; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    }

    public static class AuthorizationService {
        private String baseUrl;
        private String tokenSwapPath = "/token/swap";
        /**
         * Whether the Authorization Service (IAS) call is required for every request.
         * When {@code true} (the default), the IAS token-swap step is performed for
         * every proxied request.  Set to {@code false} to skip the inner-token
         * validation and let the MFA act as a pure session-authenticated proxy.
         */
        private boolean required = true;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getTokenSwapPath() { return tokenSwapPath; }
        public void setTokenSwapPath(String tokenSwapPath) { this.tokenSwapPath = tokenSwapPath; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
    }

    public static class Microservice {
        private String baseUrl;
        private String contextPath = "/api";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getContextPath() { return contextPath; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    }

    public static class Versioning {
        /**
         * Header from which the API version is read when
         * {@link com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.strategy.HeaderVersionExtractionStrategy}
         * is configured.
         */
        private String versionHeader = "X-API-Version";

        public String getVersionHeader() { return versionHeader; }
        public void setVersionHeader(String versionHeader) { this.versionHeader = versionHeader; }
    }

    public static class Routing {
        /**
         * When {@code true}, the version prefix ({@code /v{n}/}) and optional
         * channel segment are stripped from the path before forwarding.
         * Used by {@link com.arc_e_tect.book.sedr.mfeadapter.domain.spi.PrefixStrippingRoutingStrategy}.
         */
        private boolean stripPrefix = false;

        /**
         * Static path mappings: keys are incoming path prefixes, values are
         * the replacement prefixes to use when forwarding.
         * Used by {@link com.arc_e_tect.book.sedr.mfeadapter.domain.spi.PathMappingRoutingStrategy}.
         */
        private java.util.Map<String, String> pathMappings = new java.util.LinkedHashMap<>();

        public boolean isStripPrefix() { return stripPrefix; }
        public void setStripPrefix(boolean stripPrefix) { this.stripPrefix = stripPrefix; }
        public java.util.Map<String, String> getPathMappings() { return pathMappings; }
        public void setPathMappings(java.util.Map<String, String> pathMappings) { this.pathMappings = pathMappings; }
    }

    public static class Mtls {
        /** Whether mTLS client-certificate authentication is required. Defaults to {@code false}. */
        private boolean enabled = false;
        private String keyStore;
        private String keyStorePassword;
        private String keyStoreType = "PKCS12";
        private String trustStore;
        private String trustStorePassword;
        private String trustStoreType = "PKCS12";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getKeyStore() { return keyStore; }
        public void setKeyStore(String keyStore) { this.keyStore = keyStore; }
        public String getKeyStorePassword() { return keyStorePassword; }
        public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }
        public String getKeyStoreType() { return keyStoreType; }
        public void setKeyStoreType(String keyStoreType) { this.keyStoreType = keyStoreType; }
        public String getTrustStore() { return trustStore; }
        public void setTrustStore(String trustStore) { this.trustStore = trustStore; }
        public String getTrustStorePassword() { return trustStorePassword; }
        public void setTrustStorePassword(String trustStorePassword) { this.trustStorePassword = trustStorePassword; }
        public String getTrustStoreType() { return trustStoreType; }
        public void setTrustStoreType(String trustStoreType) { this.trustStoreType = trustStoreType; }
    }

    public static class Kafka {
        private ReferenceData referenceData = new ReferenceData();
        private Consumer consumer = new Consumer();

        public ReferenceData getReferenceData() { return referenceData; }
        public void setReferenceData(ReferenceData referenceData) { this.referenceData = referenceData; }
        public Consumer getConsumer() { return consumer; }
        public void setConsumer(Consumer consumer) { this.consumer = consumer; }

        public static class ReferenceData {
            /** Regex topic pattern to subscribe to for reference-data events. */
            private String topicPattern = "reference-data\\..*";

            public String getTopicPattern() { return topicPattern; }
            public void setTopicPattern(String topicPattern) { this.topicPattern = topicPattern; }
        }

        public static class Consumer {
            private String groupId = "mfa-reference-data";

            public String getGroupId() { return groupId; }
            public void setGroupId(String groupId) { this.groupId = groupId; }
        }
    }
}
