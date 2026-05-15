package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model carrying the HTTP response received from the microservice,
 * ready to be forwarded back to the MFE.
 */
public final class ProxiedResponse {

    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    private ProxiedResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body != null ? builder.body.clone() : new byte[0];
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body.clone();
    }

    // -----------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private int statusCode = 200;
        private Map<String, List<String>> headers = new HashMap<>();
        private byte[] body;

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
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

        public ProxiedResponse build() {
            return new ProxiedResponse(this);
        }
    }
}
