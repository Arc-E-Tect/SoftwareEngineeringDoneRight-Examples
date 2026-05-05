package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Example-scoped configuration properties for the security-mfa routing strategy.
 *
 * <p>Bound from the {@code security-mfa.*} namespace so as not to extend
 * the library-owned {@link MfeAdapterProperties}.
 *
 * <p>Example {@code application-security-mfa.yml} snippet:
 * <pre>
 * security-mfa:
 *   routing:
 *     path-pattern: "^(/v\\d+)/dutch(/.+)$"
 *     path-replacement: "$1$2"
 * </pre>
 */
@ConfigurationProperties(prefix = "security-mfa")
public class SecurityMfaProperties {

    private Routing routing = new Routing();

    public Routing getRouting() { return routing; }
    public void setRouting(Routing routing) { this.routing = routing; }

    public static class Routing {

        /**
         * Java regex pattern applied to the inbound request path.
         * Capture groups are referenced in {@link #pathReplacement}.
         */
        private String pathPattern;

        /**
         * Replacement string (supports {@code $1}, {@code $2} … back-references).
         * The resulting string becomes the path forwarded to the microservice.
         */
        private String pathReplacement;

        public String getPathPattern() { return pathPattern; }
        public void setPathPattern(String pathPattern) { this.pathPattern = pathPattern; }

        public String getPathReplacement() { return pathReplacement; }
        public void setPathReplacement(String pathReplacement) { this.pathReplacement = pathReplacement; }
    }
}
