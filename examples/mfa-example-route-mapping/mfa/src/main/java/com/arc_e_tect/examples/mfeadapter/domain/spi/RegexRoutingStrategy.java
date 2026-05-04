package com.arc_e_tect.examples.mfeadapter.domain.spi;

import com.arc_e_tect.examples.mfeadapter.domain.model.ProxiedRequest;
import com.arc_e_tect.examples.mfeadapter.infrastructure.config.RouteMappingProperties;

import java.util.regex.Pattern;

/**
 * {@link RoutingStrategy} that rewrites request paths using a configurable
 * Java regex pattern and replacement string.
 *
 * <p>The pattern and replacement are supplied via {@link RouteMappingProperties}
 * (bound from {@code route-mapping.routing.*} in the active YAML profile).
 *
 * <p>Example — strip the {@code dutch} segment inserted between the API
 * version prefix and the resource path:
 * <pre>
 *   path-pattern:     "^(/v\d+)/dutch(/.+)$"
 *   path-replacement: "$1$2"
 *
 *   /v1/dutch/familyties           → /v1/familyties
 *   /v1/dutch/familyties/lastnames/Smith → /v1/familyties/lastnames/Smith
 *   /actuator/health               → /actuator/health  (no match, unchanged)
 * </pre>
 *
 * <p>If the pattern does not match the request path the path is returned
 * unchanged, so non-API calls (e.g. {@code /actuator/health}) pass through
 * transparently.
 */
public class RegexRoutingStrategy implements RoutingStrategy {

    private final Pattern pattern;
    private final String replacement;

    public RegexRoutingStrategy(RouteMappingProperties properties) {
        this.pattern = Pattern.compile(properties.getRouting().getPathPattern());
        this.replacement = properties.getRouting().getPathReplacement();
    }

    @Override
    public String resolvePath(ProxiedRequest request) {
        return pattern.matcher(request.getPath()).replaceFirst(replacement);
    }
}
