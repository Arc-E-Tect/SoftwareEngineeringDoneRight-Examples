package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.web.filter.ApiKeyScope;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model capturing all information needed to proxy an inbound HTTP
 * request to the associated microservice.
 *
 * <p>An instance is constructed from the raw {@link jakarta.servlet.http.HttpServletRequest}
 * by the inbound web adapter and passed through the application and domain
 * layers without any further dependency on the servlet API.
 */
public final class ProxiedRequest {

    private final String method;
    private final String path;
    private final String queryString;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final String sessionId;
    private final ApiKeyScope apiKeyScope;
    private final String version;

    private ProxiedRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.queryString = builder.queryString;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body != null ? builder.body.clone() : new byte[0];
        this.sessionId = builder.sessionId;
        this.apiKeyScope = builder.apiKeyScope;
        this.version = builder.version;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body.clone();
    }

    public String getSessionId() {
        return sessionId;
    }

    /** The API-key scope granted by the API Gateway, or {@code null} if validation is disabled. */
    public ApiKeyScope getApiKeyScope() {
        return apiKeyScope;
    }

    /** The API version extracted from the request path or header, or {@code null} if none. */
    public String getVersion() {
        return version;
    }

    // -----------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String method;
        private String path;
        private String queryString;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;
        private String sessionId;
        private ApiKeyScope apiKeyScope;
        private String version;

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body != null ? body.clone() : null;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder apiKeyScope(ApiKeyScope apiKeyScope) {
            this.apiKeyScope = apiKeyScope;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public ProxiedRequest build() {
            return new ProxiedRequest(this);
        }
    }
}
