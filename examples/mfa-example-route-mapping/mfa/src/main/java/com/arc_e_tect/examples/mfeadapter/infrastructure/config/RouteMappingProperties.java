package com.arc_e_tect.examples.mfeadapter.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Example-scoped configuration properties for the route-mapping strategy.
 *
 * <p>Bound from the {@code route-mapping.*} namespace so as not to extend
 * the library-owned {@link MfeAdapterProperties}.
 *
 * <p>Example {@code application-route-mapping.yml} snippet:
 * <pre>
 * route-mapping:
 *   routing:
 *     path-pattern: "^(/v\\d+)/dutch(/.+)$"
 *     path-replacement: "$1$2"
 * </pre>
 */
@ConfigurationProperties(prefix = "route-mapping")
public class RouteMappingProperties {

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
